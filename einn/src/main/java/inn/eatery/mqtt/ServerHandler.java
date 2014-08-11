package inn.eatery.mqtt;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inn.eatery.db.MongoDBManager;
import inn.eatery.http.message.FullEncodedResponse;
import inn.eatery.http.message.Request;

public class ServerHandler extends SimpleChannelInboundHandler<MqttMessage>
{
	private final EventExecutorGroup executor;

	private String clientId;

	private String username;

	private String password;
	final MqttMessage ignoreMsg = new MqttMessage( null );

	static final Logger l = LoggerFactory.getLogger(ServerHandler.class);

	public ServerHandler(EventExecutorGroup e)
	{
		super(false);
		this.executor = e;
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final MqttMessage request) throws IOException, InstantiationException, IllegalAccessException
	{
		l.debug( "Received request for {} {}", request );
		MqttMessage err = checkState( request );
		if( err == ignoreMsg ) {
			l.info( "ignoring Msg" );
			return;
		}
		
		if( err != null ) {
			l.error( "inconsistent msg as per state {}", err );
			ctx.writeAndFlush( err );
			return;
		}

		MqttMessage resp = null;
        switch (request.fixedHeader().messageType()) {
          case CONNECT:
        	  resp = handleConnect( ctx, (MqttConnectMessage)request );
        	  break;
          case CONNACK:
          case SUBSCRIBE:
          case UNSUBSCRIBE:
          case SUBACK:
          case UNSUBACK:
          case PUBACK:
          case PUBREC:
          case PUBCOMP:
          case PUBREL:
          case PUBLISH:
          case PINGREQ:
          case PINGRESP:
          case DISCONNECT:
        	  l.error( "FIXME: handling absent for this message type" );
        	  break;
        }

        l.info("responding to it {}", resp );
		ctx.writeAndFlush( resp );
	}

	private MqttMessage checkState(MqttMessage msg)
	{
		MqttMessage invalidMsg = null;
		switch (msg.fixedHeader().messageType()) {
          case CONNECT:
        	  if( clientId != null ) {
                MqttConnAckVariableHeader vheader = new MqttConnAckVariableHeader( MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED );
                MqttFixedHeader fheader = new MqttFixedHeader( MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0 );
                invalidMsg = new MqttConnAckMessage( fheader, vheader );
        	  }
        	  break;
          case SUBSCRIBE:
          case UNSUBSCRIBE:
          case PUBLISH:
          case PUBREC:
          case PUBCOMP:
          case PUBREL:
          case DISCONNECT:

          case PINGREQ:
          case CONNACK:
          case UNSUBACK:
          case SUBACK:
          case PUBACK:
          case PINGRESP:
        	  l.error( "FIXME: handling absent for this message type" );

        	  invalidMsg = ignoreMsg;
        	  break;
        }
		return invalidMsg;
	}

	private MqttMessage handleConnect( final ChannelHandlerContext ctx, MqttConnectMessage msg)
	{
		clientId = msg.payload().clientIdentifier();
		username = msg.payload().userName();
		password = msg.payload().password();
		
		// FIXME: auth the credentials
		MqttConnAckVariableHeader vheader = new MqttConnAckVariableHeader( MqttConnectReturnCode.CONNECTION_ACCEPTED );
		MqttFixedHeader fheader = new MqttFixedHeader( MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0 );
		MqttConnAckMessage ack = new MqttConnAckMessage( fheader, vheader );
		return ack;

		/*if (path.equals("/events"))
		{
                String jsonString = httpRequest.content().toString(CharsetUtil.UTF_8);
                final JSONArray eventList;
                try
                {
                        eventList = new JSONArray(jsonString);
                }
                catch (JSONException e)
                {
                        ctx.fireExceptionCaught( e );
                        return;
                }
                Future<Boolean> future = executor.submit(new Callable<Boolean>()
                {
                        @Override
                        public Boolean call() throws Exception
                        {
                                try
                                {
                                        MongoDBManager.getInstance().insertEvent(eventList);
                                        return true;
                                }
                                catch (Exception e)
                                {
                                        l.info( "Error while inserting events!" );
                                        return false;
                                }
                        }
                });
                future.addListener(new GenericFutureListener<Future<Boolean>>()
                {
                        @Override
                        public void operationComplete(Future<Boolean> future) throws Exception
                        {
                                boolean insertSuccess = future.get();
                                if (future.isSuccess() && insertSuccess)
                                {
                                        // Build the response object
                                        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK);
                                        FullEncodedResponse encodedResponse = new FullEncodedResponse(request, httpResponse);
                                        ctx.writeAndFlush(encodedResponse);
                                }
                                else
                                {
                                        ctx.fireExceptionCaught(future.cause());
                                }
                        }
                });
                return;
		}
		else
		{
			sendBadRequest(ctx, request);
		}*/
	}

	private void sendBadRequest(ChannelHandlerContext ctx, MqttMessage request)
	{
		//ctx.writeAndFlush(encodedResponse);
	}
}
