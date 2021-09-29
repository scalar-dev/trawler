package dev.scalar.trawler.server

// import io.grpc.ManagedChannel

// @ExtendWith(VertxExtension::class)
// class TrawlerShould {
//    @Test
//    @Timeout(5, unit = TimeUnit.SECONDS)
//    fun respond(vertx: Vertx) = runBlocking(vertx.dispatcher()) {
//        vertx.deployVerticle(App()).await()
//        val channel: ManagedChannel = VertxChannelBuilder
//            .forAddress(vertx, "localhost", 9090)
//            .usePlaintext()
//            .build()
// //        val stub = TrawlerGrpcKt.TrawlerCoroutineStub(channel)
// //
// //        val response = stub.record(RecordRequest.getDefaultInstance())
//    }
// }
