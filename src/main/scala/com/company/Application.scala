package com.company

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

object Application extends App {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

  WebController.start()
}
