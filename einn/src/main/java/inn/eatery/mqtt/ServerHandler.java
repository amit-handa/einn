package inn.eatery.mqtt;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
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
