package pl.lewapek.workshop.observability.utils

object ProductsList:
  lazy val lowercaseProductsToFunFacts: Map[String, Vector[String]] =
    productsFunFacts.map((name, funFacts) => name.toLowerCase -> funFacts).toMap

  lazy val productsFunFacts: Vector[(String, Vector[String])] =
    funFacts.map(list => list.head -> list.tail.toVector).toVector

  private val funFacts = List(
    List(
      "Apple",
      "An apple tree takes about 4 to 5 years to produce its first fruit.",
      "The average apple contains around 130 calories.",
      "Apples float on water because they are 25% air.",
      "There are over 7,500 varieties of apples grown around the world.",
      "The science of apple growing is called pomology.",
      "Apples can help to clean your teeth and freshen your breath.",
      "The world's largest apple weighed over 4 pounds."
    ),
    List(
      "Banana",
      "Bananas are berries, but strawberries aren't.",
      "Bananas are naturally slightly radioactive.",
      "Bananas can help improve your mood because they contain tryptophan, which the body converts to serotonin.",
      "There are over 1,000 different types of bananas.",
      "Bananas are naturally free of fat, cholesterol, and sodium.",
      "Bananas grow on plants that are considered giant herbs, not trees.",
      "A cluster of bananas is called a hand, and a single banana is called a finger."
    ),
    List(
      "Carrot",
      "Carrots were originally purple and came in a variety of colors.",
      "Carrots were first cultivated as a medicine, not a food.",
      "Carrots are good for your eyes because they are rich in beta-carotene.",
      "The world's longest carrot measured over 20 feet.",
      "Carrots are naturally sweet and can be used to make desserts like carrot cake.",
      "Baby carrots are just regular carrots that have been cut and shaped.",
      "Carrots are members of the parsley family."
    ),
    List(
      "Grapes",
      "Grapes explode when you put them in the microwave.",
      "Grapes are used to make raisins, wine, and vinegar.",
      "Grapes are one of the oldest cultivated fruits, dating back to 6000 B.C.",
      "Concord grapes are named after Concord, Massachusetts, where they were first grown.",
      "Grapes come in different colors including green, red, black, yellow, and pink.",
      "A grapevine can live up to 100 years.",
      "Grapes contain resveratrol, which has been linked to heart health."
    ),
    List(
      "Tomato",
      "Tomatoes are fruits, but in 1893, the U.S. Supreme Court declared them vegetables.",
      "Tomatoes are rich in lycopene, a powerful antioxidant.",
      "Tomatoes were once believed to be poisonous by Europeans.",
      "The largest tomato on record weighed over 7 pounds.",
      "Tomato juice is the official state beverage of Ohio.",
      "The world's heaviest tomato plant produced over 32,000 tomatoes in a year.",
      "There are more than 10,000 varieties of tomatoes."
    ),
    List(
      "Broccoli",
      "Broccoli is a flower and part of the cabbage family.",
      "Broccoli comes from the Italian word 'brocco,' meaning arm or branch.",
      "Broccoli has been eaten by humans for over 2,000 years.",
      "It is a great source of vitamins K and C.",
      "Broccoli leaves are also edible and can be used like collard greens.",
      "The word 'broccoli' comes from the Italian word for 'cabbage sprout.'",
      "California produces 90% of the broccoli grown in the United States."
    ),
    List(
      "Orange",
      "Oranges can float on water because they contain a lot of air.",
      "Brazil is the largest producer of oranges in the world.",
      "Oranges are actually a type of berry called a hesperidium.",
      "Orange trees can live and bear fruit for over 100 years.",
      "Oranges are a hybrid of pomelo and mandarin.",
      "The color 'orange' was named after the fruit, not the other way around.",
      "Oranges are a symbol of love and marriage in many cultures."
    ),
    List(
      "Strawberry",
      "A strawberry isn't an actual berry, but a banana is.",
      "Strawberries are the only fruit with seeds on the outside.",
      "Strawberries can help whiten your teeth because they contain malic acid.",
      "There are about 200 seeds on an average strawberry.",
      "Strawberries belong to the rose family.",
      "California produces 75% of the strawberries grown in the United States.",
      "Strawberries are the first fruit to ripen in the spring."
    ),
    List(
      "Cucumber",
      "Cucumbers are 96% water and are technically a fruit.",
      "Cucumbers belong to the same family as pumpkins, zucchinis, and watermelons.",
      "Cucumbers can be up to 24 inches long, but most are harvested at 6-9 inches.",
      "Cucumbers can help reduce puffy eyes when placed on the skin.",
      "The inside of a cucumber can be up to 20 degrees cooler than the outside air.",
      "Cucumbers have been cultivated for over 3,000 years.",
      "They are a good source of vitamin K."
    ),
    List(
      "Pineapple",
      "Pineapples take almost three years to reach maturation before they are picked.",
      "Pineapples are a composite of many flowers whose individual fruitlets fuse together around a central core.",
      "Pineapples contain bromelain, an enzyme that helps with digestion.",
      "Pineapples do not ripen after being picked.",
      "The pineapple plant produces only one pineapple at a time.",
      "Pineapples were a symbol of luxury and wealth in 18th century Europe.",
      "The word 'pineapple' comes from the Spanish word 'piña,' which means pine cone."
    ),
    List(
      "Watermelon",
      "Watermelons are berries, too!",
      "The world's heaviest watermelon weighed over 350 pounds.",
      "Watermelon is 92% water.",
      "The world's largest watermelon weighed 350.5 pounds.",
      "Watermelon is a member of the gourd family.",
      "There are over 1,200 varieties of watermelon.",
      "Every part of the watermelon is edible, including the seeds and rind."
    ),
    List(
      "Blueberry",
      "Blueberries are one of the only natural foods that are truly blue in color.",
      "Blueberries are considered brain food and can improve memory.",
      "Blueberries can help reduce muscle damage after strenuous exercise.",
      "Blueberries were called 'star berries' by Native Americans because of the star-shaped blossom end.",
      "They can help improve heart health.",
      "Blueberries can be frozen without losing their nutritional value.",
      "The United States is the world's largest producer of blueberries."
    ),
    List(
      "Avocado",
      "Avocados are berries, and they're also known as alligator pears.",
      "Avocados have more potassium than bananas.",
      "Avocados are a fruit, specifically a large berry with a single seed.",
      "Avocados can take 4-6 years to bear fruit after planting.",
      "The avocado is also known as the 'butter fruit' in some parts of the world.",
      "Avocados were first cultivated in South America over 5,000 years ago.",
      "They are high in monounsaturated fats, which are heart-healthy fats."
    ),
    List(
      "Cherry",
      "There is a variety of cherry called the 'white cherry,' which is yellow.",
      "Cherries belong to the rose family.",
      "Cherries can help relieve insomnia because they contain melatonin.",
      "Cherries are related to plums and more distantly to peaches and apricots.",
      "Cherries can help reduce inflammation and pain from arthritis.",
      "There are over 1,000 different varieties of cherries.",
      "Cherries were brought to America by early settlers in the 1600s."
    ),
    List(
      "Lemon",
      "Lemons are believed to be a hybrid between a sour orange and a citron.",
      "Lemons can be used to conduct electricity.",
      "Lemons can prevent scurvy, which is caused by a deficiency of vitamin C.",
      "The lemon tree can produce fruit all year round.",
      "Lemons are believed to have originated in Assam (a region in northeast India), northern Burma, or China.",
      "Lemons were used as a remedy for scurvy by sailors in the past.",
      "The average lemon contains about 3 tablespoons of juice."
    ),
    List(
      "Kiwi",
      "Kiwi fruit originally comes from China, not New Zealand.",
      "Kiwis are also known as Chinese gooseberries.",
      "Kiwi fruits are high in vitamin C, containing more per ounce than most other fruits.",
      "Kiwi seeds can be eaten and are a good source of omega-3 fatty acids.",
      "The skin of a kiwi is edible and high in fiber.",
      "The name 'kiwi' comes from the kiwi bird, a native bird of New Zealand.",
      "Kiwi plants can live for over 50 years."
    ),
    List(
      "Mango",
      "Mangoes are the most consumed fruit in the world.",
      "Mangoes belong to the cashew family.",
      "Mangoes are related to cashews and pistachios.",
      "Mangoes are a good source of vitamins A and C.",
      "There are over 1,000 different varieties of mangoes.",
      "The mango tree can live and bear fruit for up to 300 years.",
      "Mangoes can be used to tenderize meat because they contain enzymes that break down protein."
    ),
    List(
      "Pomegranate",
      "Pomegranates are berries with hundreds of seeds called arils.",
      "Pomegranates can live for over 200 years.",
      "Pomegranates are symbols of fertility and prosperity in many cultures.",
      "Pomegranates are one of the oldest known fruits, mentioned in the Bible and other ancient texts.",
      "The seeds of a pomegranate are called arils.",
      "Pomegranates are a good source of vitamin C and potassium.",
      "Pomegranates have been used in traditional medicine for centuries."
    ),
    List(
      "Potato",
      "Potatoes are 80% water and are more energy-efficient than pasta or rice.",
      "Potatoes are the world's fourth-largest food crop.",
      "Potatoes can absorb and reflect Wi-Fi signals.",
      "Potatoes were the first vegetable to be grown in space.",
      "Potatoes were first domesticated in the region of modern-day southern Peru and northwestern Bolivia.",
      "There are over 4,000 varieties of potatoes.",
      "Potatoes are rich in vitamins C and B6, potassium, and fiber."
    ),
    List(
      "Onion",
      "Onions make you cry because they release a gas that reacts with your tears.",
      "Onions were worshipped by the ancient Egyptians.",
      "Onions can be used to create a natural dye.",
      "The sulfur compounds in onions have antibacterial properties.",
      "Onions are a good source of vitamin C, B6, and folate.",
      "There are many varieties of onions, including red, yellow, white, and green.",
      "Onions have been cultivated for over 5,000 years."
    ),
    List(
      "Spinach",
      "Spinach has more nutrients than most other vegetables.",
      "Spinach was the favorite vegetable of Catherine de Medici.",
      "Spinach is native to Persia (modern-day Iran).",
      "Spinach was brought to the U.S. by early colonists.",
      "Spinach is high in iron, which is essential for blood health.",
      "Spinach can be eaten raw or cooked and is very versatile in recipes.",
      "Spinach is a good source of vitamins A, C, and K, as well as magnesium and iron."
    ),
    List(
      "Artichoke",
      "Artichokes are flowers that are eaten before they bloom.",
      "Artichokes are one of the oldest cultivated vegetables.",
      "Artichokes are native to the Mediterranean region.",
      "Artichokes were considered an aphrodisiac in ancient Greece and Rome.",
      "Artichokes are rich in antioxidants and can help detoxify the liver.",
      "Artichokes are a good source of fiber, vitamin C, and folate.",
      "Artichoke extract is used as a natural remedy for digestive issues."
    ),
    List(
      "Zucchini",
      "Zucchinis are a type of summer squash.",
      "Zucchinis contain more potassium than a banana.",
      "Zucchini is technically a fruit, not a vegetable.",
      "Zucchini is low in calories and high in water content.",
      "Zucchini flowers are also edible and are often used in gourmet dishes.",
      "Zucchini can be eaten raw, cooked, or even baked into bread.",
      "Zucchini is low in calories and high in vitamin C and manganese."
    ),
    List(
      "Kale",
      "Kale is one of the most nutrient-dense foods on the planet.",
      "Kale was one of the most common green vegetables in Europe until the Middle Ages.",
      "Kale was one of the most common green vegetables in Europe until the end of the Middle Ages.",
      "Kale can be grown in many different climates and soil types.",
      "Kale can be eaten raw, cooked, or blended into smoothies.",
      "Kale is high in antioxidants, including quercetin and kaempferol.",
      "Kale was a staple food during World War II because it was easy to grow and highly nutritious."
    ),
    List(
      "Raspberry",
      "Raspberries are not only delicious but also a member of the rose family.",
      "Raspberries are rich in dietary fiber.",
      "Raspberries come in red, black, purple, and golden varieties.",
      "Raspberries can be used to make natural dyes.",
      "Each raspberry is made up of about 100 drupelets, each with its own seed.",
      "Raspberries can improve digestion due to their high fiber content.",
      "Raspberries have been cultivated for thousands of years, dating back to prehistoric times."
    ),
    List(
      "Cauliflower",
      "Cauliflower is a cousin of broccoli.",
      "Cauliflower comes in various colors, including purple and orange.",
      "Cauliflower can be eaten raw or cooked and is very versatile in recipes.",
      "Cauliflower can be used as a low-carb substitute for rice or pizza crust.",
      "Cauliflower is a good source of vitamins C, K, and B6.",
      "Cauliflower contains antioxidants that help reduce inflammation.",
      "Cauliflower originated in the Mediterranean region over 2,000 years ago."
    ),
    List(
      "Blackberry",
      "Blackberries are known as 'bramble fruits.'",
      "Blackberries are rich in vitamin C.",
      "Blackberries can be used to make jams, jellies, and wines.",
      "Blackberries are high in fiber, which aids in digestion.",
      "The ancient Greeks used blackberries for medicinal purposes.",
      "Blackberries can grow on thorny or thornless bushes.",
      "Blackberries are a good source of vitamins A, E, and K."
    ),
    List(
      "Peach",
      "Peaches are a member of the rose family, too.",
      "Peaches are native to northwest China.",
      "Peaches can be eaten fresh, dried, or canned.",
      "Peach trees can live for about 12 years.",
      "Peaches have been cultivated for over 4,000 years.",
      "Peaches are a good source of vitamins A and C.",
      "There are over 2,000 varieties of peaches."
    ),
    List(
      "Radish",
      "Radishes belong to the mustard family.",
      "Radishes come in a variety of colors, including black, white, and purple.",
      "Radishes are low in calories and high in vitamin C.",
      "Radishes can grow quickly, sometimes in as little as three weeks.",
      "Radish seeds can be used to make oil.",
      "Radishes have been cultivated since ancient Egypt.",
      "Radishes can be eaten raw or cooked and add a peppery flavor to dishes."
    ),
    List(
      "Plum",
      "Plums are drupes, fruits with a large hard stone inside.",
      "Plums are excellent sources of vitamins A and C.",
      "Plums can be dried to make prunes.",
      "Plum trees can produce fruit for up to 15 years.",
      "There are over 2,000 varieties of plums.",
      "Plums can help improve digestion and relieve constipation.",
      "Plums are believed to have originated in China."
    ),
    List(
      "Pear",
      "Pears are one of the few fruits that do not ripen on the tree.",
      "Pears are a good source of dietary fiber.",
      "There are over 3,000 varieties of pears worldwide.",
      "Pears were cultivated in China over 3,000 years ago.",
      "Pears can be eaten fresh, cooked, or dried.",
      "Pears are high in antioxidants, which can help reduce inflammation.",
      "The pear tree can live for over 50 years."
    ),
    List(
      "Sweet Potato",
      "Sweet potatoes are not the same as yams, although the terms are often used interchangeably.",
      "Sweet potatoes are native to Central and South America.",
      "Sweet potatoes are rich in vitamins A and C.",
      "Sweet potatoes can be orange, purple, yellow, or white in color.",
      "Sweet potatoes are a good source of dietary fiber.",
      "Sweet potatoes can help regulate blood sugar levels.",
      "Sweet potatoes were first domesticated over 5,000 years ago."
    ),
    List(
      "Asparagus",
      "Asparagus can grow up to 10 inches in a single day.",
      "Asparagus is a member of the lily family.",
      "Asparagus comes in green, white, and purple varieties.",
      "Asparagus has been cultivated for over 2,000 years.",
      "Asparagus is a good source of vitamins K and C.",
      "Asparagus can help improve digestive health.",
      "The ancient Greeks and Romans believed asparagus had medicinal properties."
    ),
    List(
      "Cantaloupe",
      "Cantaloupes are also known as muskmelons.",
      "Cantaloupes are rich in vitamins A and C.",
      "Cantaloupes are 90% water, making them very hydrating.",
      "Cantaloupes can help improve skin health due to their high vitamin A content.",
      "Cantaloupes were first cultivated in Persia around 2400 B.C.",
      "The netting on a cantaloupe's skin is a sign of ripeness.",
      "Cantaloupes can be eaten fresh, in salads, or blended into smoothies."
    ),
    List(
      "Cabbage",
      "Cabbage is 91% water.",
      "Cabbage can come in green, purple, and white varieties.",
      "Cabbage is rich in vitamins K and C.",
      "Cabbage can be fermented to make sauerkraut and kimchi.",
      "Cabbage has been cultivated for over 4,000 years.",
      "Cabbage can help improve digestion due to its high fiber content.",
      "Cabbage was used as a medicinal plant in ancient Greece and Rome."
    ),
    List(
      "Lime",
      "Limes are excellent sources of vitamin C.",
      "Limes can be used to prevent scurvy.",
      "Limes are often used to add flavor to foods and drinks.",
      "Lime trees can produce fruit year-round in tropical climates.",
      "Limes are believed to have originated in Southeast Asia.",
      "Limes are a key ingredient in many cocktails, such as the margarita.",
      "Limes can be used to clean and deodorize due to their acidic nature."
    ),
    List(
      "Ginger",
      "Ginger is a rhizome, not a root.",
      "Ginger has anti-inflammatory and antioxidant properties.",
      "Ginger can help alleviate nausea and indigestion.",
      "Ginger has been used in traditional medicine for thousands of years.",
      "Ginger can be used fresh, dried, powdered, or as an oil or juice.",
      "Ginger is related to turmeric and cardamom.",
      "Ginger can help reduce muscle pain and soreness."
    ),
    List(
      "Apricot",
      "Apricots are rich in fiber and vitamin A.",
      "Apricots are a good source of potassium.",
      "Apricots can be eaten fresh, dried, or used in cooking.",
      "Apricot trees can live and bear fruit for over 100 years.",
      "Apricots are believed to have originated in Armenia.",
      "Apricots were first cultivated in China over 4,000 years ago.",
      "Apricots are related to plums, peaches, and cherries."
    ),
    List(
      "Fig",
      "Figs are inverted flowers.",
      "Figs are one of the oldest fruits consumed by humans.",
      "Figs can help improve digestive health due to their high fiber content.",
      "Figs are rich in calcium and potassium.",
      "There are over 700 varieties of figs.",
      "Figs were considered sacred in ancient Greece and Rome.",
      "Figs can be eaten fresh, dried, or used in cooking."
    ),
    List(
      "Green Bean",
      "Green beans are technically fruits because they contain seeds.",
      "Green beans are also known as snap beans.",
      "Green beans are low in calories and high in vitamin K.",
      "Green beans can be eaten raw or cooked.",
      "Green beans were first domesticated in Central and South America.",
      "Green beans are a good source of dietary fiber.",
      "Green beans can help improve heart health due to their high antioxidant content."
    ),
    List(
      "Guava",
      "Guavas have four times more vitamin C than oranges.",
      "Guavas are native to Central America.",
      "Guavas can be eaten fresh, juiced, or used in cooking.",
      "Guava leaves can be used to make a medicinal tea.",
      "There are over 100 varieties of guava.",
      "Guavas can help improve digestion due to their high fiber content.",
      "Guavas are a good source of vitamins A and C."
    ),
    List(
      "Jackfruit",
      "Jackfruit is the largest fruit that grows on a tree.",
      "Jackfruit is the national fruit of Bangladesh.",
      "Jackfruit can weigh up to 100 pounds.",
      "Jackfruit is rich in vitamins A and C.",
      "Jackfruit can be used as a meat substitute in vegetarian dishes.",
      "Jackfruit seeds can be roasted and eaten.",
      "Jackfruit has been cultivated in India for over 6,000 years."
    ),
    List(
      "Kiwifruit",
      "Kiwi is a berry with tiny black seeds on the inside.",
      "Kiwis are rich in vitamin C.",
      "Kiwi fruits can help improve respiratory health.",
      "Kiwi fruits are native to China and were originally known as Chinese gooseberries.",
      "Kiwi fruits are a good source of dietary fiber.",
      "Kiwi fruits can help improve skin health due to their high vitamin C content.",
      "Kiwi fruits can be eaten fresh, dried, or used in cooking."
    ),
    List(
      "Lychee",
      "Lychees are part of the soapberry family.",
      "Lychees are often referred to as 'Chinese strawberries.'",
      "Lychees are rich in vitamin C and antioxidants.",
      "Lychee trees can live and bear fruit for over 100 years.",
      "Lychees can be eaten fresh, dried, or used in cooking.",
      "Lychee fruits have been cultivated in China for over 2,000 years.",
      "Lychees can help improve digestion and boost the immune system."
    ),
    List(
      "Mushroom",
      "Mushrooms are fungi, not plants.",
      "Mushrooms are a good source of vitamin D.",
      "Mushrooms can be used as a meat substitute in many dishes.",
      "There are over 10,000 known types of mushrooms.",
      "Some mushrooms have medicinal properties and are used in traditional medicine.",
      "Mushrooms can grow in the dark and do not need sunlight to thrive.",
      "Mushrooms have been cultivated for over 2,000 years."
    ),
    List(
      "Olive",
      "Olives are fruits, not vegetables, and they come from olive trees.",
      "Olive oil is a staple in Mediterranean cuisine.",
      "Olives are rich in healthy monounsaturated fats.",
      "Olive trees can live for hundreds of years.",
      "There are over 1,000 varieties of olives.",
      "Olives can be eaten fresh, cured, or used to make oil.",
      "Olives have been cultivated for over 7,000 years."
    ),
    List(
      "Papaya",
      "Papayas are rich in enzymes and used as natural meat tenderizers.",
      "Papayas are sometimes called 'the fruit of the angels.'",
      "Papayas are rich in vitamins C and A.",
      "Papaya seeds can be used as a pepper substitute.",
      "Papayas are native to Central America and southern Mexico.",
      "Papayas can help improve digestion and reduce inflammation.",
      "Papayas can be eaten fresh, juiced, or used in cooking."
    ),
    List(
      "Persimmon",
      "Persimmons are known as 'divine fruits' in ancient Greek.",
      "Persimmons can be eaten fresh, dried, or cooked.",
      "Persimmons are rich in vitamins A and C.",
      "Persimmon trees can live and bear fruit for over 50 years.",
      "There are two main types of persimmons: astringent and non-astringent.",
      "Persimmons are native to China and have been cultivated for over 2,000 years.",
      "Persimmons can help improve digestion and boost the immune system."
    ),
    List(
      "Rutabaga",
      "Rutabagas are a cross between a turnip and a cabbage.",
      "Rutabagas are often used in Scandinavian cuisine.",
      "Rutabagas are rich in vitamins C and E.",
      "Rutabagas can be eaten raw or cooked.",
      "Rutabagas were first cultivated in the 17th century.",
      "Rutabagas can help improve digestion and boost the immune system.",
      "Rutabagas are also known as swedes in many parts of the world."
    ),
    List(
      "Squash",
      "Squash belongs to the gourd family and comes in various types.",
      "Squash was one of the Three Sisters cultivated by Native American tribes.",
      "Squash is rich in vitamins A and C.",
      "Squash can be eaten raw, cooked, or used in baking.",
      "Squash has been cultivated for over 10,000 years.",
      "Squash can help improve digestion and boost the immune system.",
      "Squash comes in both summer and winter varieties."
    ),
    List(
      "Tangerine",
      "Tangerines are a type of mandarin orange.",
      "Tangerines are often called 'Christmas oranges.'",
      "Tangerines are rich in vitamin C and antioxidants.",
      "Tangerine trees can produce fruit for up to 30 years.",
      "Tangerines can help improve digestion and boost the immune system.",
      "Tangerines are native to Southeast Asia.",
      "Tangerines can be eaten fresh, juiced, or used in cooking."
    ),
    List(
      "Turnip",
      "Turnips were one of the first vegetables cultivated by humans.",
      "Turnips are a good source of vitamin C.",
      "Turnips can be eaten raw or cooked.",
      "Turnips were used as a staple food in ancient Rome and Greece.",
      "Turnips are low in calories and high in fiber.",
      "Turnips can help improve digestion and boost the immune system.",
      "Turnips have been cultivated for over 4,000 years."
    )
  )
end ProductsList
