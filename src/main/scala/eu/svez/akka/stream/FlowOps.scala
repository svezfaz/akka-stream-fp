package eu.svez.akka.stream

import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink}
import eu.svez.akka.stream.partitions.PartitionEither
import GraphDSL.Implicits._

class FlowOps {

  def carryErrors[In, Out, Err, Mat](flow: Flow[In, Either[Err, Out], Mat]):
  Flow[Either[Err, In], Either[Err, Out], Mat] = {

    Flow.fromGraph(GraphDSL.create(flow) { implicit b ⇒ flow ⇒

      val partition = b.add(PartitionEither[Err, In]())
      val inFlow    = b.add(Flow[Either[Err, In]])
      val merge     = b.add(Merge[Either[Err, Out]](2))

                    partition.out0.map(Left(_)) ~> merge
      inFlow.out ~> partition.in
                    partition.out1   ~>   flow  ~> merge

      FlowShape(inFlow.in, merge.out)
    })
  }

  def handleErrors[In, Out, Err, Mat, MatErr](flow: Flow[In, Either[Err, Out], Mat])(errorSink: Sink[Err, MatErr]):
  Flow[Either[Err, In], Either[Err, Out], (Mat, MatErr)] = {

    Flow.fromGraph(GraphDSL.create(flow, errorSink)((_, _)) { implicit b ⇒ (flow, sink) ⇒

      val partition = b.add(PartitionEither[Err, In]())
      val inFlow    = b.add(Flow[Either[Err, In]])

                    partition.out0 ~> sink
      inFlow.out ~> partition.in
                    partition.out1 ~> flow.in

      FlowShape(inFlow.in, flow.out)
    })
  }

//  def carryAudit[In, Out, Aud, Mat](flow: Flow[In, Writer[Err, Out], Mat]):
//  Flow[Either[Err, In], Either[Err, Out], Mat] = {
//
//    Flow.fromGraph(GraphDSL.create(flow) { implicit b ⇒ flow ⇒
//
//      val partition = b.add(PartitionEither[Err, In]())
//      val inFlow    = b.add(Flow[Either[Err, In]])
//      val merge     = b.add(Merge[Either[Err, Out]](2))
//
//      partition.out0.map(Left(_)) ~> merge
//      inFlow.out ~> partition.in
//      partition.out1   ~>   flow  ~> merge
//
//      FlowShape(inFlow.in, merge.out)
//    })
//  }


}
