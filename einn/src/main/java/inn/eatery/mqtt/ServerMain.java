package inn.eatery.mqtt;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inn.eatery.db.MongoDBManager;
import inn.eatery.http.exception.DefaultExceptionHandler;

/**
 * Discards any incoming data.
 */
public class ServerMain
{
	private int port;

	private static final Logger l = LoggerFactory.getLogger(ServerMain.class);

	public ServerMain(int port)
	{
		this.port = port;
	}

	public void run() throws Exception
	{
		final DefaultEventExecutorGroup group = new DefaultEventExecutorGroup(100);
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try
		{
			MongoDBManager.getInstance();
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // (3)
					.childHandler(new ChannelInitializer<SocketChannel>()
					{
						@Override
						public void initChannel(SocketChannel ch) throws Exception
						{
							ChannelPipeline chp = ch.pipeline();
							chp.addLast("decoder", new MqttDecoder());
							chp.addLast("encoder", new MqttEncoder());
							chp.addLast("mqttPayloadDecoder", new ServerHandler(group));
							chp.addLast("mqttExceptionHandler", new DefaultExceptionHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128) // (5)
					.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
			l.info("Created server on port {}", port);

			// Bind and start to accept incoming connections.
			ChannelFuture f = b.bind(port).sync(); // (7)

			// Wait until the server socket is closed.
			// In this example, this does not happen, but you can do that to gracefully
			// shut down your server.
			f.channel().closeFuture().sync();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception
	{
		int port;
		if (args.length > 0)
		{
			port = Integer.parseInt(args[0]);
		}
		else
		{
			port = 8080;
		}

		new ServerMain(port).run();
	}
}
