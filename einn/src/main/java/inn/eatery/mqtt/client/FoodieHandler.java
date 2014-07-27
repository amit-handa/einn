package inn.eatery.mqtt.client;

import java.io.IOException;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

public class FoodieHandler extends SimpleChannelInboundHandler<MqttMessage>
{
	static final Logger l = LoggerFactory.getLogger(FoodieHandler.class);
	static MqttMessage connect;
	static {
		MqttConnectVariableHeader vheader = new MqttConnectVariableHeader( );
		MqttFixedHeader iheader = new MqttFixedHeader( MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE, false, );
		connect = MqttMessageFactory.newMessage(mqttFixedHeader, variableHeader, payload);
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final MqttMessage request) throws IOException, InstantiationException, IllegalAccessException
	{
		l.info( "received message {}", request );
	}
}

