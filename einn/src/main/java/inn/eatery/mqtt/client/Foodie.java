package inn.eatery.mqtt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

import java.io.StringWriter;
import java.net.URI;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple HTTP client that prints out the content of the HTTP response to {@link System#out} to test {@link HttpSnoopServer}.
 */
public final class Foodie
{
	static final String URL = System.getProperty("url", "http://127.0.0.1:8080/events");

	static final Logger l = LoggerFactory.getLogger(Foodie.class);

	public static void main(String[] args) throws Exception
	{
		URI uri = new URI(URL);
		String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
		String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
		int port = uri.getPort();
		if (port == -1)
		{
			if ("http".equalsIgnoreCase(scheme))
			{
				port = 80;
			}
			else if ("https".equalsIgnoreCase(scheme))
			{
				port = 443;
			}
		}

		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
		{
			l.warn("Only HTTP(S) is supported.");
			return;
		}

		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try
		{
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				protected void initChannel(SocketChannel ch) throws Exception
				{
					ChannelPipeline p = ch.pipeline();
					//p.addLast(new HttpClientCodec());
					 p.addLast( "decoder", new MqttDecoder());
					 p.addLast( "encoder", new MqttEncoder());

					// Remove the following line if you don't want automatic content decompression.
					// p.addLast(new HttpContentDecompressor());

					// Uncomment the following line if you don't want to handle HttpContents.
					//p.addLast(new HttpObjectAggregator(1048576));

					p.addLast(new FoodieHandler());
				}
			});

			// Make the connection attempt.
			Channel ch = b.connect(host, port).sync().channel();

			FoodieHandler.sendConnect( ch, FoodieHandler.connect );

			// Wait for the server to close the connection.
			ch.closeFuture().sync();
			l.info("Closing Client side Connection !!!");
		}
		finally
		{
			// Shut down executor threads to exit.
			group.shutdownGracefully();
		}
	}
}
