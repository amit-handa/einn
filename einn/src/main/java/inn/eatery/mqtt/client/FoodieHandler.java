package inn.eatery.mqtt.client;

import java.io.IOException;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

public class FoodieHandler extends SimpleChannelInboundHandler<MqttMessage>
{
	static final Logger l = LoggerFactory.getLogger(FoodieHandler.class);

	static final String protocol_name = "MQIsdp";
	static final int version = 3;

	static final String clientId = "NettyClient";
	static final String username = "ahanda";
	static final String password = "ahandapwd";

	static MqttMessage connectMsg;
	static {
		MqttConnectVariableHeader vheader = new MqttConnectVariableHeader( protocol_name, version, true, true, false, 0, false, false, 200 );
		MqttFixedHeader iheader = new MqttFixedHeader( MqttMessageType.CONNECT, false, MqttQoS.AT_LEAST_ONCE, false, 0 );
		MqttConnectPayload payload = new MqttConnectPayload( clientId, null, null, username, password );
		connectMsg = MqttMessageFactory.newMessage( iheader, vheader, payload );
	}
	
	static MqttMessage ignoreMsg = new MqttMessage( null );

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final MqttMessage msg) throws IOException, InstantiationException, IllegalAccessException
	{
		l.info( "received message {}", msg );
		MqttMessage err = checkState( msg );
		if( err == ignoreMsg ) {
			l.warn( "ignoring message!" );
			return;
        }
		
		if( err != null ) {
			l.error( "inconsistent message w.r.t. the state" );
			return;
		}
		
		MqttMessage req = handleResp( ctx, msg );
	}

	private MqttMessage handleResp(ChannelHandlerContext ctx, MqttMessage msg)
	{
		MqttMessage req = null;

        switch (msg.fixedHeader().messageType()) {
          case PINGREQ:
          case CONNACK:
            if( clientId != null ) {
                MqttConnAckVariableHeader vheader = new MqttConnAckVariableHeader( MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED );
                MqttFixedHeader fheader = new MqttFixedHeader( MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0 );
                invalidMsg = new MqttConnAckMessage( fheader, vheader );
            }
            break;
          case UNSUBACK:
          case SUBACK:
          case PUBACK:
          case PINGRESP:
          case CONNECT:
          case SUBSCRIBE:
          case UNSUBSCRIBE:
          case PUBLISH:
          case PUBREC:
          case PUBCOMP:
          case PUBREL:
          case DISCONNECT:
                  l.error( "FIXME: handling absent for this message type" );

                  invalidMsg = ignoreMsg;
                  break;
        }
		return req;
	}

	private MqttMessage checkState(MqttMessage msg)
	{
        MqttMessage invalidMsg = null;
        switch (msg.fixedHeader().messageType()) {
          case PINGREQ:
          case CONNACK:
            if( clientId != null ) {
                MqttConnAckVariableHeader vheader = new MqttConnAckVariableHeader( MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED );
                MqttFixedHeader fheader = new MqttFixedHeader( MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0 );
                invalidMsg = new MqttConnAckMessage( fheader, vheader );
            }
            break;
          case UNSUBACK:
          case SUBACK:
          case PUBACK:
          case PINGRESP:
          case CONNECT:
          case SUBSCRIBE:
          case UNSUBSCRIBE:
          case PUBLISH:
          case PUBREC:
          case PUBCOMP:
          case PUBREL:
          case DISCONNECT:
                  l.error( "FIXME: handling absent for this message type" );

                  invalidMsg = ignoreMsg;
                  break;
        }
        return invalidMsg;
	}
}

