package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import pl.lewapek.workshop.observability.model.*
import zio.*
import zio.telemetry.opentelemetry.tracing.Tracing

import java.time.Instant

class InitLoadService(
  variantConfig: VariantConfig,
  sttpBackend: SttpBackendType,
  productServiceClient: ProductServiceClient,
  orderServiceClient: OrderServiceClient,
  tracing: Tracing
):

  import InitLoadService.*

  private def randomOme[A](as: IndexedSeq[A]): UIO[A] = Random.nextIntBounded(as.length).map(as.apply)
  private def randomMaxAtoB[T](a: Int, b: Int)(as: IndexedSeq[T]): UIO[Set[T]] =
    for
      n   <- Random.nextIntBetween(a, b)
      set <- ZIO.foreach(1 to n)(_ => randomOme(as)).map(_.toSet)
    yield set

  def initLoad(using TracingHeaders): IO[AppError, Unit] =
    val productIdsZIO = (1 to 10).map { _ =>
      for
        input <- randomOme(nameFunFactsTuples).map { case (name, funFact, additionalFunFact) =>
          ProductInfoInput(name, Some(funFact), Some(additionalFunFact))
        }
        product <- productServiceClient.addProduct(input)
      yield product.value.id
    }
    for
      productIds <- ZIO.collectAllPar(productIdsZIO).withParallelism(5)
      ordersZIO = (1 to 5).map { _ =>
        for
          chosenProductIds <- randomMaxAtoB(2, 10)(productIds)
          remark           <- randomOme(remarks)
          nowEpochMilli    <- Clock.instant.map(_.toEpochMilli)
          date  <- Random.nextLongBetween(nowEpochMilli - oneYearMillis, nowEpochMilli).map(Instant.ofEpochMilli)
          order <- orderServiceClient.addOrder(OrderInput(chosenProductIds.toList, Some(remark), date))
        yield ()
      }
      _ <- ZIO.collectAllParDiscard(ordersZIO).withParallelism(5)
    yield ()
    end for
  end initLoad

end InitLoadService

object InitLoadService:
  def initLoad(using TracingHeaders): ZIO[InitLoadService, AppError, Unit] =
    ZIO.serviceWithZIO[InitLoadService](_.initLoad)

  val layer                       = ZLayer.fromFunction(InitLoadService(_, _, _, _, _))
  private val oneYearMillis: Long = 366.days.toMillis

  private val nameFunFactsTuples = Vector(
    (
      "Apple",
      "An apple tree takes about 4 to 5 years to produce its first fruit.",
      "The average apple contains around 130 calories."
    ),
    ("Banana", "Bananas are berries, but strawberries aren't.", "Bananas are naturally slightly radioactive."),
    (
      "Carrot",
      "Carrots were originally purple and came in a variety of colors.",
      "Carrots were first cultivated as a medicine, not a food."
    ),
    (
      "Grapes",
      "Grapes explode when you put them in the microwave.",
      "Grapes are used to make raisins, wine, and vinegar."
    ),
    (
      "Tomato",
      "Tomatoes are fruits, but in 1893, the U.S. Supreme Court declared them vegetables.",
      "Tomatoes are rich in lycopene, a powerful antioxidant."
    ),
    (
      "Broccoli",
      "Broccoli is a flower and part of the cabbage family.",
      "Broccoli comes from the Italian word 'brocco,' meaning arm or branch."
    ),
    (
      "Orange",
      "Oranges can float on water because they contain a lot of air.",
      "Brazil is the largest producer of oranges in the world."
    ),
    (
      "Strawberry",
      "A strawberry isn't an actual berry, but a banana is.",
      "Strawberries are the only fruit with seeds on the outside."
    ),
    (
      "Cucumber",
      "Cucumbers are 96% water and are technically a fruit.",
      "Cucumbers belong to the same family as pumpkins, zucchinis, and watermelons."
    ),
    (
      "Pineapple",
      "Pineapples take almost three years to reach maturation before they are picked.",
      "Pineapples are a composite of many flowers whose individual fruitlets fuse together around a central core."
    ),
    ("Watermelon", "Watermelons are berries, too!", "The world's heaviest watermelon weighed over 350 pounds."),
    (
      "Blueberry",
      "Blueberries are one of the only natural foods that are truly blue in color.",
      "Blueberries are considered brain food and can improve memory."
    ),
    (
      "Avocado",
      "Avocados are berries, and they're also known as alligator pears.",
      "Avocados have more potassium than bananas."
    ),
    (
      "Cherry",
      "There is a variety of cherry called the 'white cherry,' which is yellow.",
      "Cherries belong to the rose family."
    ),
    (
      "Lemon",
      "Lemons are believed to be a hybrid between a sour orange and a citron.",
      "Lemons can be used to conduct electricity."
    ),
    (
      "Kiwi",
      "Kiwi fruit originally comes from China, not New Zealand.",
      "Kiwis are also known as Chinese gooseberries."
    ),
    ("Mango", "Mangoes are the most consumed fruit in the world.", "Mangoes belong to the cashew family."),
    (
      "Pomegranate",
      "Pomegranates are berries with hundreds of seeds called arils.",
      "Pomegranates can live for over 200 years."
    ),
    (
      "Potato",
      "Potatoes are 80% water and are more energy-efficient than pasta or rice.",
      "Potatoes are the world's fourth-largest food crop."
    ),
    (
      "Onion",
      "Onions make you cry because they release a gas that reacts with your tears.",
      "Onions were worshipped by the ancient Egyptians."
    ),
    (
      "Spinach",
      "Spinach has more nutrients than most other vegetables.",
      "Spinach was the favorite vegetable of Catherine de Medici."
    ),
    (
      "Artichoke",
      "Artichokes are flowers that are eaten before they bloom.",
      "Artichokes are one of the oldest cultivated vegetables."
    ),
    ("Zucchini", "Zucchinis are a type of summer squash.", "Zucchinis contain more potassium than a banana."),
    (
      "Kale",
      "Kale is one of the most nutrient-dense foods on the planet.",
      "Kale was one of the most common green vegetables in Europe until the Middle Ages."
    ),
    (
      "Raspberry",
      "Raspberries are not only delicious but also a member of the rose family.",
      "Raspberries are rich in dietary fiber."
    ),
    (
      "Cauliflower",
      "Cauliflower is a cousin of broccoli.",
      "Cauliflower comes in various colors, including purple and orange."
    ),
    ("Blackberry", "Blackberries are known as 'bramble fruits.'", "Blackberries are rich in vitamin C."),
    ("Peach", "Peaches are a member of the rose family, too.", "Peaches are native to northwest China."),
    (
      "Radish",
      "Radishes belong to the mustard family.",
      "Radishes come in a variety of colors, including black, white, and purple."
    ),
    (
      "Plum",
      "Plums are drupes, fruits with a large hard stone inside.",
      "Plums are excellent sources of vitamins A and C."
    ),
    (
      "Pear",
      "Pears are one of the few fruits that do not ripen on the tree.",
      "Pears are a good source of dietary fiber."
    ),
    (
      "Sweet Potato",
      "Sweet potatoes are not the same as yams, although the terms are often used interchangeably.",
      "Sweet potatoes are native to Central and South America."
    ),
    ("Asparagus", "Asparagus can grow up to 10 inches in a single day.", "Asparagus is a member of the lily family."),
    ("Cantaloupe", "Cantaloupes are also known as muskmelons.", "Cantaloupes are rich in vitamins A and C."),
    ("Cabbage", "Cabbage is 91% water.", "Cabbage can come in green, purple, and white varieties."),
    ("Lime", "Limes are excellent sources of vitamin C.", "Limes can be used to prevent scurvy."),
    ("Ginger", "Ginger is a rhizome, not a root.", "Ginger has anti-inflammatory and antioxidant properties."),
    ("Apricot", "Apricots are rich in fiber and vitamin A.", "Apricots are a good source of potassium."),
    ("Fig", "Figs are inverted flowers.", "Figs are one of the oldest fruits consumed by humans."),
    (
      "Green Bean",
      "Green beans are technically fruits because they contain seeds.",
      "Green beans are also known as snap beans."
    ),
    ("Guava", "Guavas have four times more vitamin C than oranges.", "Guavas are native to Central America."),
    (
      "Jackfruit",
      "Jackfruit is the largest fruit that grows on a tree.",
      "Jackfruit is the national fruit of Bangladesh."
    ),
    ("Kiwifruit", "Kiwi is a berry with tiny black seeds on the inside.", "Kiwis are rich in vitamin C."),
    ("Lychee", "Lychees are part of the soapberry family.", "Lychees are often referred to as 'Chinese strawberries.'"),
    ("Mushroom", "Mushrooms are fungi, not plants.", "Mushrooms are a good source of vitamin D."),
    (
      "Olive",
      "Olives are fruits, not vegetables, and they come from olive trees.",
      "Olive oil is a staple in Mediterranean cuisine."
    ),
    (
      "Papaya",
      "Papayas are rich in enzymes and used as natural meat tenderizers.",
      "Papayas are sometimes called 'the fruit of the angels.'"
    ),
    (
      "Persimmon",
      "Persimmons are known as 'divine fruits' in ancient Greek.",
      "Persimmons can be eaten fresh, dried, or cooked."
    ),
    (
      "Rutabaga",
      "Rutabagas are a cross between a turnip and a cabbage.",
      "Rutabagas are often used in Scandinavian cuisine."
    ),
    (
      "Squash",
      "Squash belongs to the gourd family and comes in various types.",
      "Squash was one of the Three Sisters cultivated by Native American tribes."
    ),
    ("Tangerine", "Tangerines are a type of mandarin orange.", "Tangerines are often called 'Christmas oranges.'"),
    (
      "Turnip",
      "Turnips were one of the first vegetables cultivated by humans.",
      "Turnips are a good source of vitamin C."
    )
  )
  private val remarks = Vector(
    "Exciting package, anticipation rising!",
    "Fresh surprises en route!",
    "Delightful arrival expected soon!",
    "Hoping for pleasant surprises!",
    "Ready for a tasty experience!",
    "Can't wait for the arrival!",
    "Expecting delightful surprises ahead!",
    "Fresh and tasty excitement!"
  )
end InitLoadService
