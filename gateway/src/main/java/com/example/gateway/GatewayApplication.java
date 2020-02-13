package com.example.gateway;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;

import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.cloud.gateway.config.HttpClientProperties.Pool.PoolType.DISABLED;
import static org.springframework.cloud.gateway.config.HttpClientProperties.Pool.PoolType.FIXED;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	@ConditionalOnMissingBean
	public HttpClient gatewayHttpClient(HttpClientProperties properties) {

		// configure pool resources
		HttpClientProperties.Pool pool = properties.getPool();

		ConnectionProvider connectionProvider;
		if (pool.getType() == DISABLED) {
			connectionProvider = ConnectionProvider.newConnection();
		}
		else if (pool.getType() == FIXED) {
			connectionProvider = ConnectionProvider.fixed(pool.getName(),
					pool.getMaxConnections(), pool.getAcquireTimeout(),
					pool.getMaxIdleTime());
		}
		else {
			connectionProvider = ConnectionProvider.elastic(pool.getName(),
					pool.getMaxIdleTime());
		}

		HttpClient httpClient = HttpClient.create(connectionProvider)
				.tcpConfiguration(tcpClient -> {

					if (properties.getConnectTimeout() != null) {
						tcpClient = tcpClient.option(
								ChannelOption.CONNECT_TIMEOUT_MILLIS,
								properties.getConnectTimeout());
					}

					// configure proxy if proxy host is set.
					HttpClientProperties.Proxy proxy = properties.getProxy();

					if (StringUtils.hasText(proxy.getHost())) {

						tcpClient = tcpClient.proxy(proxySpec -> {
							ProxyProvider.Builder builder = proxySpec
									.type(ProxyProvider.Proxy.HTTP)
									.host(proxy.getHost());

							PropertyMapper map = PropertyMapper.get();

							map.from(proxy::getPort).whenNonNull().to(builder::port);
							map.from(proxy::getUsername).whenHasText()
									.to(builder::username);
							map.from(proxy::getPassword).whenHasText()
									.to(password -> builder.password(s -> password));
							map.from(proxy::getNonProxyHostsPattern).whenHasText()
									.to(builder::nonProxyHosts);
						});
					}
					return tcpClient;
				});

		HttpClientProperties.Ssl ssl = properties.getSsl();
		if ((ssl.getKeyStore() != null && ssl.getKeyStore().length() > 0)
				|| ssl.getTrustedX509CertificatesForTrustManager().length > 0
				|| ssl.isUseInsecureTrustManager()) {
			httpClient = httpClient.secure(sslContextSpec -> {
				// configure ssl
				SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

				X509Certificate[] trustedX509Certificates = ssl
						.getTrustedX509CertificatesForTrustManager();
				if (trustedX509Certificates.length > 0) {
					sslContextBuilder = sslContextBuilder
							.trustManager(trustedX509Certificates);
				}
				else if (ssl.isUseInsecureTrustManager()) {
					sslContextBuilder = sslContextBuilder
							.trustManager(InsecureTrustManagerFactory.INSTANCE);
				}

				try {
					sslContextBuilder = sslContextBuilder
							.keyManager(ssl.getKeyManagerFactory());
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				sslContextSpec.sslContext(sslContextBuilder)
						.defaultConfiguration(ssl.getDefaultConfigurationType())
						.handshakeTimeout(ssl.getHandshakeTimeout())
						.closeNotifyFlushTimeout(ssl.getCloseNotifyFlushTimeout())
						.closeNotifyReadTimeout(ssl.getCloseNotifyReadTimeout())
						.handlerConfigurator(
								(handler)->{
									SSLEngine engine = handler.engine();
									//engine.setNeedClientAuth(true);
									SSLParameters params = new SSLParameters();
									List<SNIMatcher> matchers = new LinkedList<>();
									SNIMatcher matcher = new SNIMatcher(0) {

										@Override
										public boolean matches(SNIServerName serverName) {
											return true;
										}
									};
									matchers.add(matcher);
									params.setSNIMatchers(matchers);
									engine.setSSLParameters(params);
								}
						);
			});
		}

		if (properties.isWiretap()) {
			httpClient = httpClient.wiretap(true);
		}

		return httpClient;
	}


}
