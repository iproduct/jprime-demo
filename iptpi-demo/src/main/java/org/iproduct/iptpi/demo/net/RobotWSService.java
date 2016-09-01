package org.iproduct.iptpi.demo.net;

import static org.iproduct.iptpi.domain.CommandName.MOVE_RELATIVE;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.iproduct.iptpi.domain.Command;
import org.iproduct.iptpi.domain.movement.MovementCommandSubscriber;
import org.iproduct.iptpi.domain.movement.RelativeMovement;
import org.iproduct.iptpi.domain.position.PositionsFlux;
import org.reactivestreams.Publisher;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;
import reactor.ipc.netty.config.ServerOptions;
import reactor.ipc.netty.http.HttpChannel;
import reactor.ipc.netty.http.HttpServer;
import reactor.util.Logger;
import reactor.util.Loggers;

public class RobotWSService {
	private HttpServer httpServer;
	// private Timer timer;
	private TopicProcessor<Command> movementCommands;
	private PositionsFlux positions;
	private MovementCommandSubscriber movements;
	private Gson gson = new Gson();
	// private EventBus serverReactor;

	// private final Map<String,String> cache = new HashMap<>();
	private static final Charset UTF_8 = Charset.forName("utf-8");
	private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private final String MY_ADDRESS = "192.168.1.202";
	private final int MY_PORT = 80;
	private Logger log = Loggers.getLogger("RobotWSService");

	public RobotWSService(PositionsFlux positions, MovementCommandSubscriber movementSubscriber)
			throws UnknownHostException {
		this.positions = positions;
		this.movements = movementSubscriber;
		try {
			setup();
			// timer = Timer.create();
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
		// EventLoopGroup workerGroup = new NioEventLoopGroup(6);

		ServerOptions hso = ServerOptions.on(MY_ADDRESS, MY_PORT).timeoutMillis(5000);
		// .keepAlive(false)
		// .eventLoopGroup(workerGroup);
		httpServer = HttpServer.create(hso);
		// httpServer.((HttpServerSpec<Buffer,Buffer> serverSpec) ->
		// serverSpec
		//// .httpProcessor(CodecPreprocessor.from(StandardCodecs.STRING_CODEC))
		// .listen(MY_ADDRESS, MY_PORT) );
		httpServer
//				.get("/**", getStaticResourceHandler())
				// .get("/css/**", getStaticResourceHandler())
				// .get("/index.html", getStaticResourceHandler())
				// .get("/iptpi/**", getStaticResourceHandler())
				// .get("/assets/**", getStaticResourceHandler())
				// .get("/vendor/**", getStaticResourceHandler())
				// .get("/img/**", getStaticResourceHandler())
				// .get("/fonts/**", getStaticResourceHandler())
				// .get("/system-config.js", getStaticResourceHandler())
				// .get("/system-config.js.map", getStaticResourceHandler())
				// .get("/main.js", getStaticResourceHandler())
				// .get("/main.js.map", getStaticResourceHandler())
				// .get("/ember-cli-live-reload.js", getStaticResourceHandler())
				// .get("/favicon.ico", getStaticResourceHandler())
				// .get("/dashboard", getStaticResourceHandler())

				// .ws("/ws", getWsHandler())

				// httpServer.directory("/",
				// "/home/pi/.launchpi_projects/iptpi-demo/webapp/")
				// httpServer.directory("/", "src/main/webapp/")
				// httpServer.get("/**", getStaticResourceHandler())
				.start(getResourceHandler()).subscribe();
		// httpServer.startAndAwait(getStaticResourceHandler());
		// .subscribe(v-> log.info("!!!!!!!!!! SERVER STARTED!" ));
	}

	public void teardown() throws InterruptedException {
		httpServer.shutdown().subscribe(System.out::println);
		// LoggerContext loggerContext = (LoggerContext)
		// LoggerFactory.getILoggerFactory();
		// loggerContext.stop();
	}

	// private ReactorHttpHandler<Buffer, Buffer> getHandler() {
	// return channel -> {
	//// channel.headers()
	//// .entries()
	//// .forEach(
	//// entry1 -> System.out.println(String.format(
	//// "header [%s=>%s]", entry1.getKey(),
	//// entry1.getValue())));
	//// System.out.println(channel.uri());
	// String uri = channel.uri();
	// if (uri.equals("/"))
	// uri = "/index.html";
	//// String path = "src/main/webapp" + uri;
	// String path = "/home/pi/.launchpi_projects/iptpi-voxxed-demo/webapp" +
	// uri; //Pi only
	//
	// String response;
	// try {
	// response = getStaticResource(path);
	// } catch (IOException e) {
	// e.printStackTrace();
	// response = e.getMessage();
	// }
	//
	// return channel.writeWith(Flux.just(Buffer.wrap(response)));
	//// return channel.writeBufferWith(Flux.just(Buffer.wrap(response)));
	// };
	// }

	private Function<? super HttpChannel, ? extends Publisher<Void>> getResourceHandler() {
		return channel -> {
			// channel.headers()
			// .entries()
			// .forEach(
			// entry1 -> System.out.println(String.format(
			// "header [%s=>%s]", entry1.getKey(),
			// entry1.getValue())));
			System.out.println("REQUESTED: " + channel.uri());
			String uri = channel.uri();
//			uri = uri.replace("/iptpi", "/");
			
			if(uri.equals("/ws")) {
				System.out.println("Connected a websocket client: " + channel.remoteAddress());
		//					channel.headers()
		//							.entries()
		//							.forEach(
		//									entry1 -> System.out.println(String.format(
		//											"header [%s=>%s]", entry1.getKey(),
		//											entry1.getValue())));				
				return channel.flushEach().upgradeToWebsocket()
					.then(() -> {
						channel.receiveString().doOnNext(System.out::println)
						.subscribe(	
							json -> {
		//								System.out.printf(">>>>>>>>>>>>> %s WS Command Received: %s%n", Thread.currentThread(), json);
							RelativeMovement relativeMovement = gson.fromJson(json, RelativeMovement.class);
			//				Command command = new Command(MOVE_FORWARD, 
			//						new ForwardMovement(relativeMovement.getDeltaX(), relativeMovement.getVelocity()));
							Command command = new Command(MOVE_RELATIVE, relativeMovement);
							System.out.printf(">>>>>>>>>>>>> %s WS Command Received: %s%n", Thread.currentThread(), command);				
							movementCommands.onNext(command);
						});
				
		
						//signal to send initial data to client
			//			timer.submit(time -> {System.out.println("Timeout"); channelBroadcaster.onNext("");}, 100);
			
						return positions
							.doOnNext(System.out::println)
							.map( position -> gson.toJson(position) )
							.as(positionBuffer -> 
								channel.sendString(positionBuffer, UTF_8)
							);
						
					});
			} else {
//				channel.receiveString(UTF_8).subscribe(s -> System.out.println("RECEIVED: " + s));
				
				if (uri.equals("/") || uri.indexOf('.') == -1)
					uri = "/index.html";
				// else if(uri.equals("/css/custom.css"))
				// uri = "/css/custom.css";
				// else {
				// log.info("Resource not found: " + channel.uri());
				// return channel.status(404).sendString(Mono.just("Resource not
				// found: " + channel.uri()), Charset.forName("UTF-8"));
				// }
	
				String contentType;
				switch (uri.substring(uri.lastIndexOf('.') + 1)) { // resolve
																	// content type
																	// by file
																	// extension
				case "html":
					contentType = "text/html;charset=utf-8";
					break;
				case "css":
					contentType = "text/css";
					break;
				case "js":
					contentType = "application/javascript";
					break;
				case "map":
					contentType = "application/json";
					break;
				case "jpg":
					contentType = "image/jpeg";
					break;
				case "png":
					contentType = "image/png";
					break;
				case "gif":
					contentType = "image/gif";
					break;
				case "ico":
					contentType = "image/x-icon";
					break;
				default:
					contentType = "text/plain";
				}
	
				// System.out.println("Content-Type: " + contentType);
				// String path = "src/main/webapp" + uri;
				String path = "/home/pi/.launchpi_projects/iptpi-demo/webapp/dist" + uri; // Pi
				// String path = "src/main/webapp" + uri; // Laptop
				// System.out.println("Path: " + path); // only
	
				// ByteBuf responseBuffer;
				// try {
				// responseBuffer = getStaticResource(path);
				// } catch (IOException e) {
				// e.printStackTrace();
				// responseBuffer =
				// Unpooled.wrappedBuffer(e.getMessage().getBytes());
				// }
				// System.out.println("Content-Length: " +
				// responseBuffer.readableBytes());
	
				Mono<Void> result = channel.status(200).addResponseHeader("Content-Type", contentType)
						// .addResponseHeader("Content-Length",
						// responseBuffer.readableBytes() + "")
						.addResponseHeader("Connection", "close").removeTransferEncodingChunked()
						// .send(Mono.just(responseBuffer));
						.sendFile(new File(path));
	
				// channel.delegate().flush();
				// result.subscribe(System.out::println);
				// channel.headers().remove("transfer-encoding");
				return result;
	
				// return channel.writeBufferWith(Flux.just(Buffer.wrap(response)));
			}
		};
	}

	private Function<? super HttpChannel, ? extends Publisher<Void>> getWsHandler() {
		return channel -> {
			System.out.println("Connected a websocket client: " + channel.remoteAddress());
			// channel.headers()
			// .entries()
			// .forEach(
			// entry1 -> System.out.println(String.format(
			// "header [%s=>%s]", entry1.getKey(),
			// entry1.getValue())));
			return channel.flushEach().upgradeToWebsocket().then(() -> {
				channel.receiveString().doOnNext(System.out::println).subscribe(json -> {
					// System.out.printf(">>>>>>>>>>>>> %s WS Command Received:
					// %s%n", Thread.currentThread(), json);
					RelativeMovement relativeMovement = gson.fromJson(json, RelativeMovement.class);
					// Command command = new Command(MOVE_FORWARD,
					// new ForwardMovement(relativeMovement.getDeltaX(),
					// relativeMovement.getVelocity()));
					Command command = new Command(MOVE_RELATIVE, relativeMovement);
					System.out.printf(">>>>>>>>>>>>> %s WS Command Received: %s%n", Thread.currentThread(), command);
					movementCommands.onNext(command);
				});

				// signal to send initial data to client
				// timer.submit(time -> {System.out.println("Timeout");
				// channelBroadcaster.onNext("");}, 100);

				return positions.doOnNext(System.out::println).map(position -> gson.toJson(position))
						.as(positionBuffer -> channel.sendString(positionBuffer, UTF_8));

			});
		};

	}

	private ByteBuf getStaticResource(String fileName) throws IOException {
		Path filePath = Paths.get(fileName);
		System.out.println(fileName);
		return Unpooled.wrappedBuffer(Files.readAllBytes(filePath));
	}

}