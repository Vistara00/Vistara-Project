package com.example.vistaraapp.entities_dataclass

import com.example.vistaraapp.R

data class UniqueAnimal(// this defines information each animal must have.
    val id: Int,
    val name: String,
    val scientificName: String,
    val imageRes: Int,
    val description: String,
    val funFact: String,
    val bestTimeToSee: String
)

val uniqueAnimals = listOf(
    // BIG FIVE
    UniqueAnimal(
        id = 1,
        name = "Lion",
        scientificName = "Panthera leo",
        imageRes = R.drawable.lala,
        description = "The 'King of the Jungle' - Nairobi National Park has a healthy lion population. These majestic big cats are often seen lounging in the sun or hunting during early morning hours.",
        funFact = "A lion's roar can be heard from 8 kilometers away!",
        bestTimeToSee = "Early morning (6am - 9am)"
    ),
    UniqueAnimal(
        id = 2,
        name = "African Elephant",
        scientificName = "Loxodonta africana",
        imageRes = R.drawable.elephant,
        description = "The largest land animal on Earth. Nairobi Park has a growing elephant population that moves between the park and the Kitengela dispersal area.",
        funFact = "Elephants can recognize themselves in mirrors and have excellent memory!",
        bestTimeToSee = "Late morning (10am - 12pm)"
    ),
    UniqueAnimal(
        id = 3,
        name = "Black Rhino",
        scientificName = "Diceros bicornis",
        imageRes = R.drawable.rhino,
        description = "Critically endangered species. Nairobi National Park is one of the few places in Kenya where you can see black rhinos in their natural habitat.",
        funFact = "Rhino horns are made of keratin, the same material as human hair and fingernails!",
        bestTimeToSee = "Early morning (6am - 8am)"
    ),
    UniqueAnimal(
        id = 4,
        name = "Leopard",
        scientificName = "Panthera pardus",
        imageRes = R.drawable.leopard,
        description = "Elusive and powerful. Leopards are masters of camouflage and one of the most sought-after sightings in the park.",
        funFact = "Leopards can carry prey twice their body weight up a tree!",
        bestTimeToSee = "Early morning or late evening"
    ),
    UniqueAnimal(
        id = 5,
        name = "Buffalo",
        scientificName = "Syncerus caffer",
        imageRes = R.drawable.buffalo,
        description = "Known as 'Black Death' - one of the most dangerous animals in Africa. Buffaloes move in large herds within the park.",
        funFact = "Buffaloes have excellent memory and can hold grudges against hunters!",
        bestTimeToSee = "Late afternoon (4pm - 6pm)"
    ),

    //OTHER MAMMALS
    UniqueAnimal(
        id = 6,
        name = "Giraffe",
        scientificName = "Giraffa camelopardalis",
        imageRes = R.drawable.girraffe,
        description = "The tallest mammal on Earth. Nairobi Park has a healthy population of Masai giraffes with their distinctive irregular spots.",
        funFact = "Giraffes have the same number of neck vertebrae as humans (7), but each can be up to 10 inches long!",
        bestTimeToSee = "Throughout the day"
    ),
    UniqueAnimal(
        id = 7,
        name = "Zebra",
        scientificName = "Equus quagga",
        imageRes = R.drawable.zebraa,
        description = "Common zebras with their iconic black and white stripes. Often seen in large herds grazing on the plains.",
        funFact = "Every zebra has a unique stripe pattern, like human fingerprints - no two are alike!",
        bestTimeToSee = "Mid-morning (9am - 11am)"
    ),
    UniqueAnimal(
        id = 8,
        name = "Cheetah",
        scientificName = "Acinonyx jubatus",
        imageRes = R.drawable.cheetah,
        description = "The fastest land animal. Nairobi Park is one of the best places to see cheetahs in their natural habitat.",
        funFact = "Cheetahs can accelerate from 0 to 100 km/h in just 3 seconds - faster than most sports cars!",
        bestTimeToSee = "Early morning (6am - 8am)"
    ),
    UniqueAnimal(
        id = 9,
        name = "Hippopotamus",
        scientificName = "Hippopotamus amphibius",
        imageRes = R.drawable.h,
        description = "Large semi-aquatic mammals found in the park's dams and rivers. They spend most of the day in water to keep cool.",
        funFact = "Despite their size, hippos can run up to 30 km/h on land!",
        bestTimeToSee = "Late afternoon when they leave water"
    ),

    // ANTELOPES
    UniqueAnimal(
        id = 10,
        name = "Eland",
        scientificName = "Taurotragus oryx",
        imageRes = R.drawable.e,
        description = "The largest antelope species in Africa. Their gentle nature and impressive size make them a park favorite.",
        funFact = "Elands can jump up to 2.5 meters high from a standing position!",
        bestTimeToSee = "Morning and late afternoon"
    ),
    UniqueAnimal(
        id = 11,
        name = "Hartebeest",
        scientificName = "Alcelaphus buselaphus",
        imageRes = R.drawable.harte,
        description = "Known for their unusual elongated face and distinctive horns. Often seen grazing in small herds.",
        funFact = "Hartebeests are one of the fastest antelopes, reaching speeds up to 70 km/h!",
        bestTimeToSee = "Throughout the day"
    ),
    UniqueAnimal(
        id = 12,
        name = "Impala",
        scientificName = "Aepyceros melampus",
        imageRes = R.drawable.impa,
        description = "Graceful antelopes with distinctive 'M' marking on their hindquarters. Very common and widespread in the park.",
        funFact = "Impalas are excellent jumpers, leaping up to 10 meters in distance and 3 meters high!",
        bestTimeToSee = "Mid-day (11am - 2pm)"
    ),
    UniqueAnimal(
        id = 13,
        name = "Grant's Gazelle",
        scientificName = "Nanger granti",
        imageRes = R.drawable.gaze,
        description = "Medium-sized antelope with lyre-shaped horns. Known for their speed and agility.",
        funFact = "Grant's gazelles can survive without drinking water for long periods, getting moisture from plants!",
        bestTimeToSee = "Early morning"
    ),

    //  BIRDS
    UniqueAnimal(
        id = 14,
        name = "Ostrich",
        scientificName = "Struthio camelus",
        imageRes = R.drawable.ostri,
        description = "The world's largest bird. Often seen striding across the open plains of Nairobi National Park.",
        funFact = "Ostriches have the largest eyes of any land animal - about 5 cm in diameter!",
        bestTimeToSee = "Morning and evening"
    ),
    UniqueAnimal(
        id = 15,
        name = "Secretary Bird",
        scientificName = "Sagittarius serpentarius",
        imageRes = R.drawable.secretary,
        description = "A large bird of prey with eagle-like body on crane-like legs. Stalks the grasslands hunting for snakes.",
        funFact = "Secretary birds are known for their snake-hunting technique - they stomp their prey to death!",
        bestTimeToSee = "Morning hours"
    ),

    // OTHER
    UniqueAnimal(
        id = 16,
        name = "Warthog",
        scientificName = "Phacochoerus africanus",
        imageRes = R.drawable.warthog,
        description = "Famous for their facial warts and kneeling grazing posture. Often seen with their tails sticking straight up.",
        funFact = "Warthogs run with their tails straight up like an antenna, making them easy to spot!",
        bestTimeToSee = "Throughout the day"
    ),
    UniqueAnimal(
        id = 17,
        name = "Spotted Hyena",
        scientificName = "Crocuta crocuta",
        imageRes = R.drawable.hyna,
        description = "Often misunderstood but highly intelligent predators. They play a crucial role in the park's ecosystem.",
        funFact = "Spotted hyenas have one of the strongest bite forces of any mammal!",
        bestTimeToSee = "Early morning and dusk"
    )
)