package eu.ill.visa.vdi.exceptions;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ExceptionListenerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultExceptionListener extends ExceptionListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionListener.class);

    @Override
    public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
        log.error("onEventException: {}", e.getMessage(), e);
    }

    @Override
    public void onDisconnectException(Exception e, SocketIOClient client) {
        log.error("onDisconnectException: {}", e.getMessage(), e);
    }

    @Override
    public void onConnectException(Exception e, SocketIOClient client) {
        log.error("onConnectException: {}", e.getMessage(), e);
    }

    @Override
    public void onPingException(Exception e, SocketIOClient client) {
        log.error("onPingException: {}", e.getMessage(), e);
    }

    @Override
    public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        log.error("exceptionCaught: {}", e.getMessage(), e);
        return true;
    }

    @Override
    public void onAuthException(Throwable e, SocketIOClient socketIOClient) {
        log.error("onAuthException: {}", e.getMessage(), e);
    }

}
