object Test extends App {
  val x = 1
  val y: x.type = 1

  implicitly[x.type =:= y.type]
  implicitly[1      =:= y.type]
  implicitly[1.type =:= x.type]
}
