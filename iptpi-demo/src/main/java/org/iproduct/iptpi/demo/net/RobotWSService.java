package org.iproduct.iptpi.demo.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.iproduct.iptpi.domain.Command;
import org.iproduct.iptpi.domain.movement.MovementData;
import org.iproduct.iptpi.domain.movement.ForwardMovement;
import org.iproduct.iptpi.domain.movement.MovementCommandSubscriber;
import org.iproduct.iptpi.domain.movement.RelativeMovement;
import org.iproduct.iptpi.domain.position.PositionFluxion;
import org.omg.CORBA.Environment;

import com.google.gson.Gson;

import reactor.core.publisher.Flux;
import reactor.core.publisher.TopicProcessor;
import reactor.core.subscriber.Subscribers;
import reactor.core.timer.Timer;
import reactor.io.buffer.Buffer;
import reactor.io.net.Spec.HttpServerSpec;
import reactor.rx.Broadcaster;
import reactor.rx.net.NetStreams;
import reactor.rx.net.http.ReactorHttpHandler;
import reactor.rx.net.http.ReactorHttpServer;

import static org.iproduct.iptpi.domain.CommandName.*;

public class RobotWSService {
	private ReactorHttpServer<Buffer, Buffer> httpServer;
	private Environment env;
	private Timer timer;
	private TopicProcessor<Command> movementCommands;
	private PositionFluxion positions;
	private MovementCommandSubscriber movements;
	private Gson gson = new Gson();
//	private EventBus serverReactor;
	
//	private final Map<String,String> cache = new HashMap<>();
	private static final Charset UTF_8 = Charset.forName("utf-8");
	private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private final String MY_ADDRESS = "192.168.0.108";
	private final int MY_PORT = 80;

	public RobotWSService(PositionFluxion positions, MovementCommandSubscriber movementSubscriber) throws UnknownHostException {
		this.positions = positions;
		this.movements = movementSubscriber;
		try {
			setup();
			timer = Timer.create();
			movementCommands = TopicProcessor.create();
			movementCommands.subscribe(movementSubscriber);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void setup() throws InterruptedException {
		setupServer();
		
	}

	private void setupServer() throws InterruptedException {
		httpServer = NetStreams.<Buffer, Buffer>httpServer((HttpServerSpec<Buffer,Buffer> serverSpec) -> 
			serverSpec
//				.httpProcessor(CodecPreprocessor.from(StandardCodecs.STRING_CODEC))
				.listen(MY_ADDRESS, MY_PORT) );
		httpServer.get("/", getStaticResourceHandler());
		httpServer.get("/index.html", getStaticResourceHandler());
		httpServer.get("/app/**", getStaticResourceHandler());
		httpServer.get("/node_modules/**", getStaticResourceHandler());
		httpServer.get("/js/**", getStaticResourceHandler());
		httpServer.get("/css/**", getStaticResourceHandler());
		httpServer.get("/img/**", getStaticResourceHandler());
		httpServer.get("/fonts/**", getStaticResourceHandler());
		httpServer.ws("/ws", getWsHandler());

		httpServer.start().subscribe(Subscribers.consumer(System.out::println));
	}

	public void teardown() throws InterruptedException {
		httpServer.shutdown().after().subscribe(Subscribers.consumer(System.out::println));
//		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//		loggerContext.stop();
	}

//	private ReactorHttpHandler<Buffer, Buffer> getHandler() {
//		return channel -> {
////			channel.headers()
////					.entries()
////					.forEach(
////							entry1 -> System.out.println(String.format(
////									"header [%s=>%s]", entry1.getKey(),
////									entry1.getValue())));
////			System.out.println(channel.uri());
//			String uri = channel.uri();
//			if (uri.equals("/")) 
//				uri = "/index.html";
////			String path = "src/main/webapp" + uri;	
//			String path = "/home/pi/.launchpi_projects/iptpi-voxxed-demo/webapp" + uri;	//Pi only
//			
//			String response;
//			try {
//				response = getStaticResource(path);
//			} catch (IOException e) {
//				e.printStackTrace();
//				response = e.getMessage();
//			}
//		
//			return channel.writeWith(Flux.just(Buffer.wrap(response)));
////			return channel.writeBufferWith(Flux.just(Buffer.wrap(response)));
//		};
//	}

	private ReactorHttpHandler<Buffer, Buffer> getStaticResourceHandler() {
		return channel -> {
//			channel.headers()
//					.entries()
//					.forEach(
//							entry1 -> System.out.println(String.format(
//									"header [%s=>%s]", entry1.getKey(),
//									entry1.getValue())));
//			System.out.println(channel.uri());
			String uri = channel.uri();
			if (uri.equals("/")) 
				uri = "/index.html";
			
			String contentType;
			switch(uri.substring(uri.lastIndexOf('.')+1)) { //resolve content type by file extension
				case "html": contentType = "text/html"; break;
				case "css": contentType = "text/css"; break;
				case "js": contentType = "application/javascript"; break;
				case "jpg": contentType = "image/jpeg"; break;
				case "png": contentType = "image/png"; break;
				case "gif": contentType = "image/gif"; break;
				default: contentType = "text/plain";
			}
			
//			String path = "src/main/webapp" + uri;	
			String path = "/home/pi/.launchpi_projects/iptpi-demo/webapp" + uri;	//Pi only
			
			Buffer responseBuffer;
			try {
				responseBuffer = getStaticResource(path);
			} catch (IOException e) {
				e.printStackTrace();
				responseBuffer = Buffer.wrap(e.getMessage());
			}
			
			return channel
				.responseHeader("Content-Type", contentType)
				.writeWith(Flux.just(responseBuffer));
//			return channel.writeBufferWith(Flux.just(Buffer.wrap(response)));
		};
	}

	private ReactorHttpHandler<Buffer, Buffer> getWsHandler() {
		return channel -> {
			System.out.println("Connected a websocket client: " + channel.remoteAddress());
//			channel.headers()
//					.entries()
//					.forEach(
//							entry1 -> System.out.println(String.format(
//									"header [%s=>%s]", entry1.getKey(),
//									entry1.getValue())));				
			channel.map(Buffer::asString).consume(		
				json -> {
//				System.out.printf(">>>>>>>>>>>>> %s WS Command Received: %s%n", Thread.currentThread(), json);
				RelativeMovement relativeMovement = gson.fromJson(json, RelativeMovement.class);
				Command command = new Command(MOVE_FORWARD, 
						new ForwardMovement(relativeMovement.getDeltaX(), relativeMovement.getVelocity()));
				System.out.printf(">>>>>>>>>>>>> %s WS Command Received: %s%n", Thread.currentThread(), command);				
				movementCommands.onNext(command);
			});
			
	
			//signal to send initial data to client
//			timer.submit(time -> {System.out.println("Timeout"); channelBroadcaster.onNext("");}, 100);

			return positions.flatMap(position -> 
				channel.writeWith(
					Flux.just(Buffer.wrap(gson.toJson(position)))
				));			
		};
	}


	private  Buffer getStaticResource(String fileName) throws IOException{
		Path filePath = Paths.get(fileName);
		return Buffer.wrap(Files.readAllBytes(filePath));
	}

}