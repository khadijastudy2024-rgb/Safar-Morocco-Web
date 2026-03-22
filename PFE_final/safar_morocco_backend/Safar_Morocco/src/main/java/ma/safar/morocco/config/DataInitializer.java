package ma.safar.morocco.config;

import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.event.entity.EvenementCulturel;
import ma.safar.morocco.event.repository.EvenementCulturelRepository;
import ma.safar.morocco.media.entity.Media;
import ma.safar.morocco.review.entity.Avis;
import ma.safar.morocco.review.repository.AvisRepository;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import ma.safar.morocco.offer.repository.OfferRepository;
import ma.safar.morocco.offer.entity.Offer;
import ma.safar.morocco.offer.enums.OfferType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Configuration
@SuppressWarnings({"java:S1192", "java:S107", "java:S3776", "java:S1199"})
public class DataInitializer {

        private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

        private final UtilisateurRepository userRepository;
        private final DestinationRepository destinationRepository;
        private final AvisRepository reviewRepository;
        private final EvenementCulturelRepository eventRepository;
        private final PasswordEncoder encoder;
        private final DataExpansionService dataExpansionService;
        private final OfferRepository offerRepository;

        public DataInitializer(UtilisateurRepository userRepository,
                        DestinationRepository destinationRepository,
                        AvisRepository reviewRepository,
                        EvenementCulturelRepository eventRepository,
                        PasswordEncoder encoder,
                        DataExpansionService dataExpansionService,
                        OfferRepository offerRepository) {
                this.userRepository = userRepository;
                this.destinationRepository = destinationRepository;
                this.reviewRepository = reviewRepository;
                this.eventRepository = eventRepository;
                this.encoder = encoder;
                this.dataExpansionService = dataExpansionService;
                this.offerRepository = offerRepository;
        }

        @Bean
        @Transactional
        CommandLineRunner initDatabase() {
                return args -> {
                        try {
                                // Delegate repair and missing file checking to the service
                                dataExpansionService.repairData();

                                /*
                                 * =======================
                                 * 1. CREATE USERS
                                 * =======================
                                 */
                                if (!userRepository.existsByEmail("admin@safar.com")) {
                                        Utilisateur admin = Utilisateur.builder()
                                                        .nom("Admin")
                                                        .email("admin@safar.com")
                                                        .motDePasseHache(encoder.encode(System.getenv("ADMIN_PASS") != null ? System.getenv("ADMIN_PASS") : "SecureSafarMrc2026!Adm"))
                                                        .role("ADMIN")
                                                        .telephone("+212600000000")
                                                        .langue("Morocco") // Mapping 'Country' to 'langue' as
                                                                           // placeholder or just logic adaptation
                                                        .description("System Administrator for Safar Morocco.")
                                                        .actif(true)
                                                        .compteBloquer(false)
                                                        .build();
                                        userRepository.save(admin);
                                }

                                if (!userRepository.existsByEmail("user@safar.com")) {
                                        Utilisateur user = Utilisateur.builder()
                                                        .nom("John Doe") // Combined First/Last name
                                                        .email("user@safar.com")
                                                        .motDePasseHache(encoder.encode(System.getenv("USER_PASS") != null ? System.getenv("USER_PASS") : "SecureSafarMrc2026!Usr"))
                                                        .role("USER")
                                                        .telephone("+15550123")
                                                        .langue("USA")
                                                        .description("Travel enthusiast exploring Morocco.")
                                                        .actif(true)
                                                        .compteBloquer(false)
                                                        .build();
                                        userRepository.save(user);
                                }

                                Utilisateur normalUser = userRepository.findByEmail("user@safar.com").orElseThrow();

                                /*
                                 * =======================
                                 * 2. CREATE DESTINATIONS
                                 * =======================
                                 */
                                List<Destination> destinations = new ArrayList<>();
                                destinations.add(createDestination(
                                                "Jemaa el-Fnaa", "Place Jemaa el-Fnaa", "ساحة جامع الفناء", "Plaza Jemaa el-Fnaa",
                                                "Jemaa el-Fnaa square is the beating heart of Marrakech since its foundation in the 11th century by the Almoravids. Historically, it served as a place of public justice, hence its name which means \"assembly of the dead\".",
                                                "La place Jemaa el-Fnaa est le cœur battant de Marrakech depuis la fondation de la ville au XIe siècle par les Almoravides. Historiquement, elle servait de lieu de justice publique, d'où son nom qui signifierait \"assemblée des morts\".",
                                                "تعتبر ساحة جامع الفناء القلب النابض لمدينة مراكش منذ تأسيس المدينة في القرن الحادي عشر على يد المرابطين. تاريخياً، كانت الساحة مكاناً للقضاء العام، ومن هنا جاء اسمها الذي يعني \"مجمع الأموات\".",
                                                "La plaza Jemaa el-Fnaa es el corazón palpitante de Marrakech desde la fundación de la ciudad en el siglo XI por los almorávides. Históricamente, sirvió como lugar de justicia pública, de ahí su nombre que significa \"asamblea de los muertos\".",
                                                "Marrakech", "Marrakech", "مراكش", "Marrakech",
                                                "Cultural", 31.6258, -7.9891,
                                                "https://images.openai.com/static-rsc-3/4oMlZ5fHrx1GAh246lmd7hDI8EiHK8zCRcb14tTvYq9PUvGmFUUkgIg6UfwsrMAeypv8Qh2dGKWJK24qEi5cOwZeF0KS2g3Zq8SmdpV_LdY?purpose=fullsize&v=1",
                                                "Famous square and market place.", "Célèbre place et marché.", "ساحة وسوق شهير.", "Famosa plaza y mercado.",
                                                "October - May", "Octobre - Mai", "أكتوبر - مايو", "Octubre - Mayo",
                                                "Arabic, French, Tashelhit", "Arabe, Français, Tashelhit", "العربية، الفرنسية، التشلحيت", "Árabe, Francés, Tashelhit",
                                                50.0, 15000L));

                                destinations.add(createDestination(
                                                "Hassan II Mosque", "Mosquée Hassan II", "مسجد الحسن الثاني", "Mezquita Hassan II",
                                                "The Hassan II Mosque, built over the Atlantic Ocean in Casablanca, is a monumental masterpiece of contemporary Islamic architecture. Completed in 1993, it symbolizes the rebirth of traditional Moroccan craftsmanship.",
                                                "La Mosquée Hassan II, érigée sur les flots de l'Océan Atlantique à Casablanca, est un chef-d'œuvre monumental de l'architecture islamique contemporaine. Achevée en 1993, elle symbolise la renaissance de l'artisanat traditionnel marocain.",
                                                "يعد مسجد الحسن الثاني، الذي بني فوق المحيط الأطلسي في الدار البيضاء، شاهداً معمارياً ضخماً على العمارة الإسلامية المعاصرة. تم الانتهاء منه في عام 1993، وهو يرمز إلى نهضة الحرف التقليدية المغربية.",
                                                "La Mezquita Hassan II, construida sobre el Océano Atlántico en Casablanca, es una obra maestra monumental de la arquitectura islámica contemporánea. Terminada en 1993, simboliza el renacimiento de la artesanía tradicional marroquí.",
                                                "Casablanca", "Casablanca", "الدار البيضاء", "Casablanca",
                                                "Religious", 33.608, -7.632,
                                                "https://images.openai.com/static-rsc-3/NgTc0N8_ERJ20iUdgG9ZwfSn0lJ3A1RqPZCLMUDzEAvYjyFfakYWOYGLLcITEJPGwjcvLb9yK6_DXlyPaFaKc6kx699pSMHjrBJ2-U5sg3w?purpose=fullsize&v=1",
                                                "Largest mosque in Morocco.", "La plus grande mosquée du Maroc.", "أكبر مسجد في المغرب.", "La mezquita más grande de Marruecos.",
                                                "Year-round", "Toute l'année", "طوال العام", "Todo el año",
                                                "Arabic, French", "Arabe, Français", "العربية، الفرنسية", "Árabe, Francés",
                                                120.0, 12000L));

                                destinations.add(createDestination(
                                                "Ait Ben Haddou", "Aït Ben Haddou", "آيت بن حدو", "Ait Ben Haddou",
                                                "Ait Ben Haddou is a traditional pre-Saharan habitat. The buildings are grouped within defensive walls, reinforced by corner towers.",
                                                "Aït Ben Haddou est un habitat pré-saharien traditionnel. Les bâtiments sont regroupés à l'intérieur de murs défensifs, renforcés par des tours d'angle.",
                                                "آيت بن حدو هو سكن تقليدي في منطقة ما قبل الصحراء. يتم تجميع المباني داخل أسوار دفاعية، مدعمة بأبراج ركنية.",
                                                "Ait Ben Haddou es un hábitat tradicional presahariano. Los edificios se agrupan dentro de muros defensivos, reforzados por torres de esquina.",
                                                "Ouarzazate", "Ouarzazate", "ورزازات", "Uarzazate",
                                                "Historical", 31.047, -7.1306,
                                                "https://images.openai.com/static-rsc-3/PsvMnYdCkrxUuGwDw1Lnb8SKvMfWqed_wad_0XfZxHL9W4U82igx5uAAGlm1lmQod1xJQL_SswvBgREqosPJa7yv1Pr2BIH6i7KygyPcYvs?purpose=fullsize&v=1",
                                                "UNESCO World Heritage Site.", "Site classé au patrimoine mondial de l'UNESCO.", "موقع التراث العالمي لليونسكو.", "Sitio del Patrimonio Mundial de la UNESCO.",
                                                "September - May", "Septembre - Mai", "سبتمبر - مايو", "Septiembre - Mayo",
                                                "Arabic, Tashelhit", "Arabe, Tashelhit", "العربية، التشلحيت", "Árabe, Tashelhit",
                                                80.0, 8000L));

                                destinations.add(createDestination(
                                                "Merzouga Desert", "Désert de Merzouga", "صحراء مرزوكة", "Desierto de Merzouga",
                                                "The Merzouga desert, with its imposing dunes of Erg Chebbi which can reach up to 150 meters in height, is a geological wonder of south-eastern Morocco.",
                                                "Le désert de Merzouga, avec ses imposantes dunes d'Erg Chebbi qui peuvent atteindre jusqu'à 150 mètres de hauteur, est une merveille géologique du sud-est marocain.",
                                                "تعد صحراء مرزوكة، بجبالها الرملية الضخمة في عرق الشبي التي قد يصل ارتفاعها إلى 150 متراً، أعجوبة جيولوجية في الجنوب الشرىقي المغربي.",
                                                "El desierto de Merzouga, con sus imponentes dunas de Erg Chebbi que pueden alcanzar hasta 150 metros de altura, es una maravilla geológica del sureste de Marruecos.",
                                                "Errachidia", "Errachidia", "الرشيدية", "Errachidia",
                                                "Nature", 31.0994, -4.0127,
                                                "https://images.openai.com/static-rsc-3/QSwTCw_J-gISrD2F5XLFmLfBk9bpsBNqiMEa9q3frLYK_iU_KTOcnYsFG7zCLSUV8KWnJgZzwZq7zueRMArW0DpNPCZTNiVvjFIvAF_5ebI?purpose=fullsize&v=1",
                                                "Gateway to Sahara Desert.", "Porte du désert du Sahara.", "بوابة الصحراء الكبرى.", "Puerta del desierto del Sahara.",
                                                "October - March", "Octobre - Mars", "أكتوبر - مارس", "Octubre - Marzo",
                                                "Arabic, Hassanya, Berber", "Arabe, Hassanya, Berbère", "العربية، الحسانية، الأمازيغية", "Árabe, Hassanya, Berber",
                                                450.0, 5000L));

                                destinations.add(createDestination(
                                                "Ouzoud Waterfalls", "Cascades d'Ouzoud", "شلالات أوزود", "Cascadas de Ouzoud",
                                                "The Ouzoud waterfalls, nestled in the heart of the majestic Middle Atlas mountains, are one of the most spectacular natural attractions in North Africa.",
                                                "Les cascades d'Ouzoud, nichées au cœur des majestueuses montagnes du Moyen Atlas, constituent l'une des attractions naturelles les plus spectaculaires d'Afrique du Nord.",
                                                "تعتبر شلالات أوزود، الواقعة في قلب جبال الأطلس المتوسط الشاهقة، واحدة من أكثر المعالم الطبيعية إثارة للإعجاب في شمال أفريقيا.",
                                                "Las cascadas de Ouzoud, situadas en el corazón de las majestuosas montañas del Atlas Medio, son una de las atracciones naturales más espectaculares del norte de África.",
                                                "Azilal", "Azilal", "أزيلال", "Azilal",
                                                "Nature", 32.0142, -6.7189,
                                                "https://images.openai.com/static-rsc-3/tUf-hSbp7EoJfy0auuvI3MiB6O2c55fJFKKe_pWZzNCuu_5WwR2uk8ozJZxLq0yfDY_xB8zkzig-rcj6TB96ea0frKPwnOcHzTrYSfjh48w?purpose=fullsize&v=1",
                                                "Famous waterfalls in Atlas.", "Célèbres cascades de l'Atlas.", "شلالات شهيرة في الأطلس.", "Famosas cascadas del Atlas.",
                                                "March - May", "Mars - Mai", "مارس - مايو", "Marzo - Mayo",
                                                "Arabic, Tashelhit", "Arabe, Tashelhit", "العربية، التشلحيت", "Árabe, Tashelhit",
                                                100.0, 7000L));
                                destinations.add(createDestination(
                                                "Fes El Bali", "Fès El Bali", "فاس البالي", "Fes El Bali",
                                                "Fes el-Bali, the oldest walled part of Fes, was founded at the end of the 8th century. It is a UNESCO World Heritage site and one of the world's largest car-free urban areas.",
                                                "Fès el-Bali, la plus ancienne médina fortifiée de Fès, a été fondée à la fin du VIIIe siècle. C'est un site classé au patrimoine mondial de l'UNESCO et l'une des plus vastes zones piétonnes au monde.",
                                                "فاس البالي، أقدم جزء محصن في فاس، تأسست في نهاية القرن الثامن. إنها موقع تراث عالمي لليونسكو وواحدة من أكبر المناطق الحضرية الخالية من السيارات في العالم.",
                                                "Fes el-Bali, la parte amurallada más antigua de Fes, fue fundada a finales del siglo VIII. Es un sitio del Patrimonio Mundial de la UNESCO y una de las zonas urbanas sin coches más grandes del mundo.",
                                                "Fes", "Fès", "فاس", "Fez",
                                                "Cultural", 34.0331, -5.0003,
                                                "https://www.story-rabat.com/wp-content/uploads/2024/05/fes-el-bali1.webp",
                                                "Ancient medina of Fes.", "Ancienne médina de Fès.", "مدينة فاس القديمة.", "Antigua medina de Fez.",
                                                "September - May", "Septembre - Mai", "سبتمبر - مايو", "Septiembre - Mayo",
                                                "Arabic, French", "Arabe, Français", "العربية، الفرنسية", "Árabe, Francés",
                                                60.0, 10000L));

                                destinations.add(createDestination(
                                                "Todgha Gorges", "Gorges du Todgha", "مضايق تودغى", "Gargantas del Todra",
                                                "The Todgha Gorges are a series of limestone river canyons in the eastern part of the High Atlas Mountains.",
                                                "Les gorges du Todgha se composent de falaises de calcaire rouge monumentales atteignant jusqu'à 300 mètres de hauteur dans le Haut Atlas.",
                                                "مضايق تودغى هي سلسلة من الأخاديد النهرية الجيرية في الجزء الشرقي من جبال الأطلس الكبير.",
                                                "Las Gargantas del Todra son una serie de cañones fluviales de piedra caliza en la parte oriental de las montañas del Atlas Alto.",
                                                "Tinghir", "Tinghir", "تنغير", "Tinerhir",
                                                "Nature", 31.5873, -5.5764,
                                                "https://www.travel-exploration.com/images/Todra-Gorge-Travel-Exploration-Morocco_17vshgxu7v8z2.jpeg",
                                                "Spectacular canyon.", "Canyon spectaculaire.", "أخدود مذهل.", "Cañón espectacular.",
                                                "September - May", "Septembre - Mai", "سبتمبر - مايو", "Septiembre - Mayo",
                                                "Arabic, Tashelhit", "Arabe, Tashelhit", "العربية، التشلحيت", "Árabe, Tashelhit",
                                                150.0, 4000L));

                                destinations.add(createDestination(
                                                "Dades Valley", "Vallée du Dadès", "وادي دادس", "Valle del Dades",
                                                "The Dades Valley is known as the \"Valley of a Thousand Kasbahs\" for the many fortifications that dot the landscape.",
                                                "La Vallée du Dadès, surnommée \"la vallée des mille kasbahs\", se fraie un passage au milieu d'étonnants affleurements rocheux.",
                                                "يُعرف وادي دادس باسم \"وادي الألف قصبة\" بسبب كثرة التحصينات التي تطبع المشهد الطبيعي.",
                                                "El Valle del Dades es conocido como el \"Valle de las mil Kasbahs\" por las numerosas fortificaciones que salpican el paisaje.",
                                                "Boumalne Dades", "Boumalne Dades", "بومالن دادس", "Boumalne Dades",
                                                "Nature", 31.4575, -5.9937,
                                                "https://www.morocco-ecotours.com/wp-content/uploads/2019/05/THE-DADES-VALLEY-FROM-OUARZAZATE.jpg",
                                                "Rock formations and valleys.", "Formations rocheuses et vallées.", "تكوينات صخرية ووديان.", "Formaciones rocosas y valles.",
                                                "March - May", "Mars - Mai", "مارس - مايو", "Marzo - Mayo",
                                                "Arabic, Tashelhit", "Arabe, Tashelhit", "العربية، التشلحيت", "Árabe, Tashelhit",
                                                150.0, 3500L));

                                destinations.add(createDestination(
                                                "Essaouira Medina", "Médina d'Essaouira", "مدينة الصويرة", "Medina de Essaouira",
                                                "The Essaouira medina, formerly known as Mogador, is a fortified coastal jewel famous for its marine winds and vibrant culture.",
                                                "La charmante médina d'Essaouira, anciennement connue sous l'illustre nom de Mogador, est un joyau côtier fortifié.",
                                                "تعتبر مدينة الصويرة، المعروفة سابقاً باسم موغادور، جوهرة ساحلية محصنة تشتهر برياحها البحرية وثقافتها النابضة بالحياة.",
                                                "La medina de Essaouira, anteriormente conocida como Mogador, es una joya costera fortificada famosa por sus vientos marinos y su cultura vibrante.",
                                                "Essaouira", "Essaouira", "الصويرة", "Essaouira",
                                                "Cultural", 31.5085, -9.7595,
                                                "https://tse1.mm.bing.net/th/id/OIP.z42bHPRS20luELBtPWucuwHaE8?w=474&h=379&c=7&p=0",
                                                "Coastal fortified city.", "Ville côtière fortifiée.", "مدينة ساحلية محصنة.", "Ciudad costera fortificada.",
                                                "September - June", "Septembre - Juin", "سبتمبر - يونيو", "Septiembre - Junio",
                                                "Arabic, French, Tashelhit", "Arabe, Français, Tashelhit", "العربية، الفرنسية، التشلحيت", "Árabe, Francés, Tashelhit",
                                                90.0, 9500L));
                                destinations.add(createDestination(
                                                "Agadir Beach", "Plage d'Agadir", "شاطئ أكادير", "Playa de Agadir",
                                                "Agadir beach, stretching along Morocco's sparkling Atlantic coast, is a modern seaside resort known for its wide sandy shores and year-round sunshine.",
                                                "La splendeur de la cité balnéaire d'Agadir, située le long du littoral atlantique, offre une plage de sable fin s'étendant sur plusieurs kilomètres.",
                                                "يتميز شاطئ وأكادير، الممتد على طول ساحل المحيط الأطلسي المتلألئ في المغرب، بكونه منتجعاً بحرياً حديثاً يشتهر بشواطئه الرملية الواسعة وشمسه المشرقة طوال العام.",
                                                "La playa de Agadir, que se extiende a lo largo de la brillante costa atlántica de Marruecos, es un moderno centro turístico costero conocido por sus amplias costas arenosas y su sol durante todo el año.",
                                                "Agadir", "Agadir", "أكادير", "Agadir",
                                                "Nature", 30.4278, -9.5981,
                                                "https://www.barcelo.com/guia-turismo/wp-content/uploads/2024/09/ok-playas-de-agadir.jpg",
                                                "Modern seaside resort.", "Station balnéaire moderne.", "منتجع سياحي حديث.", "Moderno centro turístico costero.",
                                                "Year-round", "Toute l'année", "طوال العام", "Todo el año",
                                                "Arabic, French, Tashelhit", "Arabe, Français, Tashelhit", "العربية، الفرنسية، التشلحيت", "Árabe, Francés, Tashelhit",
                                                150.0, 13000L));

                                destinations.add(createDestination(
                                                "Kelaat Mgouna", "Kelaat M'Gouna", "قلعة مكونة", "Kelaat M'Gouna",
                                                "Kalâat M'Gouna, located in the High Atlas, is famous for its \"Valley of Roses\" and the annual festival dedicated to these flowers.",
                                                "Kalâat M'Gouna, située dans le Haut Atlas, est réputée pour sa \"Vallée des Roses\" et son festival annuel.",
                                                "تشتهر قلعة مكونة، الواقعة في الأطلس الكبير، بـ \"وادي الورود\" والمهرجان السنوي المخصص لهذه الزهور.",
                                                "Kelaat M'Gouna, situada en el Alto Atlas, es famosa por su \"Valle de las Rosas\" y el festival anual dedicado a estas flores.",
                                                "Kelaat Mgouna", "Kelaat Mgouna", "قلعة مكونة", "Kelaat Mgouna",
                                                "Nature", 31.2381, -6.1264,
                                                "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0d/cc/c6/d2/la-vallee-des-rose.jpg?w=600&h=-1&s=1",
                                                "The Valley of Roses.", "La Vallée des Roses.", "وادي الورود.", "El Valle de las Rosas.",
                                                "April - May", "Avril - Mai", "أبريل - مايو", "Abril - Mayo",
                                                "Arabic, Tashelhit", "Arabe, Tashelhit", "العربية، التشلحيت", "Árabe, Tashelhit",
                                                70.0, 2500L));

                                destinations.add(createDestination(
                                                "Sefrou City", "Ville de Sefrou", "مدينة صفرو", "Ciudad de Sefrou",
                                                "Sefrou is a charming town at the foot of the Middle Atlas, known for its cherry festival and its ancient Jewish quarter.",
                                                "Sefrou est une ville charmante au pied du Moyen Atlas, connue pour son festival des cerises et son ancien quartier juif.",
                                                "صفرو هي مدينة ساحرة عند قدم الأطلس المتوسط، تشتهر بمهرجان حب الملوك وحيها اليهودي القديم.",
                                                "Sefrou es una ciudad encantadora a los pies del Atlas Medio, conocida por su festival de las cerezas y su antiguo barrio judío.",
                                                "Sefrou", "Sefrou", "صفرو", "Sefrou",
                                                "Nature", 33.829, -4.8329,
                                                "https://aujourdhui.ma/wp-content/uploads/2016/08/sefrou.jpg",
                                                "Garden city of Morocco.", "Ville jardin du Maroc.", "مدينة الحدائق في المغرب.", "Ciudad jardín de Marruecos.",
                                                "Spring", "Printemps", "الربيع", "Primavera",
                                                "Arabic, French", "Arabe, Français", "العربية، الفرنسية", "Árabe, Francés",
                                                50.0, 2000L));

                                destinations.add(createDestination(
                                                "Guelmim Gate", "Porte de Guelmim", "باب كلميم", "Puerta de Guelmim",
                                                "Guelmim, known as the \"gateway to the desert\", was historically a major terminal for trans-Saharan caravans.",
                                                "Guelmim, surnommée \"la porte du désert\", était historiquement un terminal majeur pour les caravanes transsahariennes.",
                                                "تعتبر كلميم، المعروفة بـ \"باب الصحراء\"، تاريخياً محطة رئيسية للقوافل العابرة للصحراء.",
                                                "Guelmim, conocida como la \"puerta del desierto\", fue históricamente una terminal importante para las caravanas transaharianas.",
                                                "Guelmim", "Guelmim", "كلميم", "Guelmim",
                                                "Cultural", 28.987, -10.0574,
                                                "https://static.lematin.ma/files/lematin/images/articles/2021/03/90a8d5194085b18624e2c31105483454.jpg",
                                                "Gateway to the desert.", "Porte du désert.", "بوابة الصحراء.", "Puerta del desierto.",
                                                "Winter", "Hiver", "الشتاء", "Invierno",
                                                "Arabic, Hassanya", "Arabe, Hassanya", "العربية، الحسانية", "Árabe, Hassanya",
                                                100.0, 1500L));

                                destinations.add(createDestination(
                                                "Laayoune City", "Ville de Laâyoune", "مدينة العيون", "Ciudad de El Aaiún",
                                                "Laayoune is the largest city in the Sahara, founded in the 1930s as a Spanish colonial garrison.",
                                                "Laâyoune est la plus grande ville du Sahara, fondée dans les années 1930 comme garnison coloniale espagnole.",
                                                "العيون هي أكبر مدينة في الصحراء، تأسست في الثلاثينيات كحامية استعمارية إسبانية.",
                                                "El Aaiún es la ciudad más grande del Sahara, fundada en la década de 1930 como guarnición colonial española.",
                                                "Laayoune", "Laâyoune", "العيون", "El Aaiún",
                                                "Cultural", 27.15, -13.1991,
                                                "https://tse2.mm.bing.net/th/id/OIP.Fsuvp-qsQ5AvmMV5ZU5o8QHaE8?w=474&h=379&c=7&p=0",
                                                "Largest city in Sahara.", "Plus grande ville du Sahara.", "أكبر مدينة في الصحراء.", "La ciudad más grande del Sahara.",
                                                "Winter", "Hiver", "الشتاء", "Invierno",
                                                "Arabic, Hassanya, Spanish", "Arabe, Hassanya, Espagnol", "العربية، الحسانية، الإسبانية", "Árabe, Hassanya, Español",
                                                120.0, 1200L));

                                destinations.add(createDestination(
                                                "Imilchil Village", "Village d'Imilchil", "قرية إميلشيل", "Pueblo de Imilchil",
                                                "The legendary mountain village of Imilchil is famous for its \"engagement festival\" and its twin lakes.",
                                                "Le légendaire village de montagne d'Imilchil est célèbre pour son \"moussem des fiançailles\" et ses lacs jumeaux.",
                                                "تعتبر قرية إملشيل الجبلية الأسطورية شهيرة بـ \"موسم الخطوبة\" وببحيراتها التوأم.",
                                                "El legendario pueblo de montaña de Imilchil es famoso por su \"festival de compromiso\" y sus lagos gemelos.",
                                                "Imilchil", "Imilchil", "إميلشيل", "Imilchil",
                                                "Nature", 32.1583, -5.5833,
                                                "https://tse3.mm.bing.net/th/id/OIP.i74I8UQKPR9miDguNbfvJQHaEH?w=474&h=379&c=7&p=0",
                                                "High Atlas mountain village.", "Village de montagne du Haut Atlas.", "قرية في جبال الأطلس الكبير.", "Pueblo de montaña del Alto Atlas.",
                                                "September", "Septembre", "سبتمبر", "Septiembre",
                                                "Berber, Arabic", "Berbère, Arabe", "الأمازيغية، العربية", "Berber, Árabe",
                                                180.0, 1800L));

                                destinations.add(createDestination(
                                                "Dakhla Lagoon", "Lagune de Dakhla", "داخلة لاغون", "Laguna de Dajla",
                                                "Dakhla is a world-class kitesurfing destination, where the desert meets the ocean in a spectacular lagoon.",
                                                "Dakhla est une destination mondiale de kitesurf, où le désert rencontre l'océan dans une lagune spectaculaire.",
                                                "تعتبر الداخلة وجهة عالمية لركوب الأمواج، حيث يلتقي الصحراء بالمحيط في بحيرة مذهلة.",
                                                "Dajla es un destino mundial de kitesurf, donde el desierto se encuentra con el océano en una laguna espectacular.",
                                                "Dakhla", "Dakhla", "الداخلة", "Dajla",
                                                "Nature", 23.6841, -15.9579,
                                                "https://tse3.mm.bing.net/th/id/OIP.qCKfiG-jps-hjQyZrdFY1QHaEK?w=474&h=379&c=7&p=0",
                                                "Paradise for kitesurfing.", "Paradis pour le kitesurf.", "جنة لمحبي ركوب الأمواج.", "Paraíso para el kitesurf.",
                                                "September - April", "Septembre - Avril", "سبتمبر - أبريل", "Septiembre - Abril",
                                                "Arabic, Hassanya, Spanish", "Arabe, Hassanya, Espagnol", "العربية، الحسانية، الإسبانية", "Árabe, Hassanya, Español",
                                                500.0, 4500L));

                                destinations.add(createDestination(
                                                "Al Hoceima Bay", "Baie d'Al Hoceima", "خليج الحسيمة", "Bahía de Alhucemas",
                                                "Al Hoceima, pearl of the Mediterranean, is known for its turquoise waters and its history of Rifian resistance.",
                                                "Al Hoceima, perle de la Méditerranée, est connue pour ses eaux turquoises et son histoire de résistance rifaine.",
                                                "الحسيمة، لؤلؤة البحر الأبيض المتوسط، معروفة بمياها الفيروزية وتاريخها في المقاومة الريفية.",
                                                "Alhucemas, perla del Mediterráneo, es conocida por sus aguas turquesas y su historia de resistencia rifeña.",
                                                "Al Hoceima", "Al Hoceima", "الحسيمة", "Alhucemas",
                                                "Nature", 35.2446, -3.9321,
                                                "https://images.ferryhopper.com/locations/al-hoceima-port-morocco.jpg",
                                                "Mediterranean pearl.", "Perle de la Méditerranée.", "لؤلؤة المتوسط.", "Perla del Mediterráneo.",
                                                "June - August", "Juin - Août", "يونيو - أغسطس", "Junio - Agosto",
                                                "Arabic, Spanish, Tarifit", "Arabe, Espagnol, Tarifit", "العربية، الإسبانية، التاريفيت", "Árabe, Español, Tarifit",
                                                150.0, 3000L));

                                destinations.add(createDestination(
                                                "Bin El Ouidane Lake", "Lac de Bin El Ouidane", "بحيرة بين الويدان", "Lago Bin El Ouidane",
                                                "Bin el Ouidane is a magnificent artificial lake in the Atlas, famous for its emerald waters and fishing.",
                                                "Bin El Ouidane est un magnifique lac artificiel dans l'Atlas, célèbre pour ses eaux émeraude.",
                                                "بين الويدان هي بحيرة اصطناعية رائعة في الأطلس، تشتهر بمياهها الزمردية وصيد الأسماك.",
                                                "Bin El Ouidane es un magnífico lago artificial en el Atlas, famoso por sus aguas esmeralda y la pesca.",
                                                "Azilal", "Azilal", "أزيلال", "Azilal",
                                                "Nature", 32.1333, -6.2833,
                                                "https://cdn.britannica.com/89/144989-050-F66A32CA/Laayoune-Western-Sahara.jpg",
                                                "Magnificent Atlas lake.", "Magnifique lac de l'Atlas.", "بحيرة رائعة في الأطلس.", "Magnífico lago del Atlas.",
                                                "Spring - Autumn", "Printemps - Automne", "الربيع - الخريف", "Primavera - Otoño",
                                                "Arabic, French", "Arabe, Français", "العربية، الفرنسية", "Árabe, Francés",
                                                250.0, 2200L));

                                destinations.add(createDestination(
                                                "Oualidia Lagoon", "Lagune de Oualidia", "الوليدية لاغون", "Laguna de Oualidia",
                                                "Oualidia is a peaceful lagoon on the Atlantic coast, the oyster capital of Morocco.",
                                                "Oualidia est une paisible lagune sur la côte atlantique, la capitale de l'huître au Maroc.",
                                                "الوليدية هي بحيرة هادئة على ساحل المحيط الأطلسي، عاصمة المحار في المغرب.",
                                                "Oualidia es una laguna tranquila en la costa atlántica, la capital de la ostra en Marruecos.",
                                                "Oualidia", "Oualidia", "الوليدية", "Oualidia",
                                                "Nature", 32.73, -9.04,
                                                "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/12/d7/d9/dd/die-lagune.jpg?w=900&h=-1&s=1",
                                                "Seaside oyster capital.", "Capitale de l'huître.", "عاصمة المحار.", "Capital de la ostra.",
                                                "Year-round", "Toute l'année", "طوال العام", "Todo el año",
                                                "Arabic, French", "Arabe, Français", "العربية، الفرنسية", "Árabe, Francés",
                                                300.0, 2800L));

                                destinations.add(createDestination(
                                                "Azrou Cedar Forest", "Forêt de Cèdres d'Azrou", "غابة أرز أزرو", "Bosque de Cedros de Azrou",
                                                "The majestic cedar forest near Azrou is home to the Barbary macaque and ancient trees.",
                                                "La majestueuse forêt de cèdres près d'Azrou abrite les macaques de Barbarie et des arbres millénaires.",
                                                "تأوي غابة الأرز المهيبة بالقرب من أزرو قرود الماكاك البربري والأشجار المعمرة.",
                                                "El majestuoso bosque de cedros cerca de Azrou alberga macacos de Berbería y árboles milenarios.",
                                                "Azrou", "Azrou", "أزرو", "Azrou",
                                                "Nature", 33.4417, -5.2158,
                                                "https://img.freepik.com/premium-photo/azrou-cedar-forest-morocco_480416-507.jpg",
                                                "Home to Barbary macaques.", "Habitat des macaques.", "موطن الماكاك البربري.", "Hogar de los macacos de Berbería.",
                                                "Spring - Autumn", "Printemps - Automne", "الربيع - الخريف", "Primavera - Otoño",
                                                "Berber, Arabic, French", "Berbère, Arabe, Français", "الأمازيغية، العربية، الفرنسية", "Berber, Árabe, Francés",
                                                40.0, 3200L));

                                destinations.add(createDestination(
                                                "Hercules Caves", "Grottes d'Hercule", "مغارة هرقل", "Cuevas de Hércules",
                                                "The enigmatic Hercules Caves, near Tangier, are steeped in legends and offer a view over the Atlantic.",
                                                "Les énigmatiques Grottes d'Hercule, près de Tanger, sont imprégnées de légendes et offrent une vue sur l'Atlantique.",
                                                "مغارة هرقل الغامضة، بالقرب من طنجة، غارقة في الأساطير وتوفر إطلالة على المحيط الأطلسي.",
                                                "Las enigmáticas Cuevas de Hércules, cerca de Tánger, están impregnadas de leyendas y ofrecen una vista sobre el Atlántico.",
                                                "Tangier", "Tanger", "طنجة", "Tánger",
                                                "Historical", 35.76, -5.9392,
                                                "https://www.barcelo.com/guia-turismo/wp-content/uploads/2022/01/cuevas-de-hercules.jpg",
                                                "Mythological caves.", "Grottes mythologiques.", "مغارات أسطورية.", "Cuevas mitológicas.",
                                                "April - September", "Avril - Septembre", "أبريل - سبتمبر", "Abril - Septiembre",
                                                "Arabic, French, Spanish", "Arabe, Français, Espagnol", "العربية، الفرنسية، الإسبانية", "Árabe, Francés, Español",
                                                20.0, 6000L));

                                destinations.add(createDestination(
                                                "Oukaimeden Resort", "Station d'Oukaïmeden", "منتجع أوكايمدن", "Estación de Oukaimeden",
                                                "Oukaimeden is the highest ski resort in Africa, offering a unique winter experience in the Atlas.",
                                                "Oukaïmeden est la plus haute station de ski d'Afrique, offrant une expérience hivernale unique dans l'Atlas.",
                                                "أوكايمدن هو أعلى منتجع للتزلج في أفريقيا، ويقدم تجربة شتوية فريدة في الأطلس.",
                                                "Oukaimeden es la estación de esquí más alta de África y ofrece una experiencia invernal única en el Atlas.",
                                                "Marrakech", "Marrakech", "مراكش", "Marrakech",
                                                "Nature", 31.2044, -7.8631,
                                                "https://www.lesjardinsdelamedina.com/blog/wp-content/uploads/2019/11/OUKA%C3%8FMEDEN-678x381.jpg",
                                                "Highest ski resort in Africa.", "Plus haute station de ski d'Afrique.", "أعلى منتجع للتزلج في أفريقيا.", "Estación de esquí más alta de África.",
                                                "January - March", "Janvier - Mars", "يناير - مارس", "Enero - Marzo",
                                                "Berber, Arabic, French", "Berbère, Arabe, Français", "الأمازيغية، العربية، الفرنسية", "Berber, Árabe, Francés",
                                                350.0, 3800L));

                                destinations.add(createDestination(
                                                "Saidia Beach", "Plage de Saïdia", "شاطئ السعيدية", "Playa de Saïdia",
                                                "Saidia, the \"Blue Pearl\" of the Mediterranean, is famous for its long sandy beach and turquoise waters.",
                                                "Saïdia, la \"Perle Bleue\" de la Méditerranée, est célèbre pour sa longue plage de sable et ses eaux turquoises.",
                                                "السعيدية، \"اللؤلؤة الزرقاء\" للبحر الأبيض المتوسط، مشهورة بشاطئها الرملي الطويل ومياها الفيروزية.",
                                                "Saïdia, la \"Perla Azul\" del Mediterráneo, es famosa por su larga playa de arena y sus aguas turquesas.",
                                                "Berkane", "Berkane", "بركان", "Berkane",
                                                "Nature", 35.0833, -2.2333,
                                                "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0a/c2/e8/36/photo-de-la-plage-de.jpg?w=1100&h=1100&s=1",
                                                "The Blue Pearl of the Med.", "La Perle Bleue de la Méditerranée.", "اللؤلؤة الزرقاء للمتوسط.", "La Perla Azul del Mediterráneo.",
                                                "June - September", "Juin - Septembre", "يونيو - سبتمبر", "Junio - Septiembre",
                                                "Arabic, French", "Arabe, Français", "العربية، الفرنسية", "Árabe, Francés",
                                                180.0, 5500L));

                                destinations.add(createDestination(
                                                "Asilah Old Town", "Vieille ville d'Asilah", "مدينة أصيلة القديمة", "Casco antiguo de Asilah",
                                                "Asilah is a bright and white medina on the Atlantic, known for its murals and its arts festival.",
                                                "Asilah est une médina blanche et lumineuse sur l'Atlantique, connue pour ses peintures murales et son festival des arts.",
                                                "أصيلة هي مدينة بيضاء ومشرقة على المحيط الأطلسي، معروفة بجدارياتها ومهرجانها الفني.",
                                                "Asilah es una medina blanca y luminosa en el Atlántico, conocida por sus murales y su festival de artes.",
                                                "Asilah", "Asilah", "أصيلة", "Asilah",
                                                "Cultural", 35.4667, -6.0333,
                                                "https://chicmorocco.com/wp-content/uploads/2023/08/vistas-asilah-muralla-1024x683.jpg",
                                                "Coastal art town.", "Ville d'art côtière.", "مدينة الفن الساحلية.", "Ciudad de arte costera.",
                                                "June - September", "Juin - Septembre", "يونيو - سبتمبر", "Junio - Septiembre",
                                                "Arabic, Spanish, French", "Arabe, Espagnol, Français", "العربية، الإسبانية، الفرنسية", "Árabe, Español, Francés",
                                                120.0, 4200L));

                                // Saving all destinations (filtering nulls which mean they already exist)
                                List<Destination> toSave = destinations.stream()
                                                .filter(java.util.Objects::nonNull)
                                                .toList();
                                List<Destination> savedDestinations = destinationRepository.saveAll(toSave);

                                // Add Reviews
                                if (savedDestinations.size() >= 2) {
                                        Avis r1 = Avis.builder()
                                                        .note(5)
                                                        .commentaire("Amazing place!")
                                                        .auteur(normalUser)
                                                        .destination(savedDestinations.get(0))
                                                        .status("APPROVED")
                                                        .datePublication(LocalDateTime.now())
                                                        .build();

                                        Avis r2 = Avis.builder()
                                                        .note(4)
                                                        .commentaire("Very beautiful.")
                                                        .auteur(normalUser)
                                                        .destination(savedDestinations.get(1))
                                                        .status("APPROVED")
                                                        .datePublication(LocalDateTime.now())
                                                        .build();

                                        reviewRepository.saveAll(List.of(r1, r2));
                                }

                                /*
                                 * =======================
                                 * UPDATE EVENT DATES TO FUTURE (2026)
                                 * =======================
                                 */
                                {
                                        Map<String, int[]> eventDateOffsets = new HashMap<>();
                                        eventDateOffsets.put("Mawazine", new int[] { 30, 38 });
                                        eventDateOffsets.put("Timitar", new int[] { 45, 47 });
                                        eventDateOffsets.put("Fes Sacred Music", new int[] { 60, 68 });
                                        eventDateOffsets.put("Rose Festival", new int[] { 15, 18 });
                                        eventDateOffsets.put("Tanjazz", new int[] { 120, 122 });
                                        eventDateOffsets.put("Film Festival", new int[] { 150, 158 });
                                        eventDateOffsets.put("Cherry Festival", new int[] { 35, 37 });
                                        eventDateOffsets.put("Camel Festival", new int[] { 90, 93 });
                                        eventDateOffsets.put("Sahara Marathon", new int[] { 7, 9 });
                                        eventDateOffsets.put("Imilchil Marriage", new int[] { 125, 127 });
                                        eventDateOffsets.put("Tan-Tan Moussem", new int[] { 42, 48 });
                                        eventDateOffsets.put("Cedar Festival", new int[] { 52, 54 });
                                        eventDateOffsets.put("Kitesurf World Cup", new int[] { 110, 115 });
                                        eventDateOffsets.put("FICAM Meknes", new int[] { 20, 25 });

                                        for (Map.Entry<String, int[]> entry : eventDateOffsets.entrySet()) {
                                                String evName = entry.getKey();
                                                int[] offsets = entry.getValue();
                                                eventRepository.findByNomEn(evName).ifPresent(ev -> {
                                                        ev.setDateDebut(LocalDateTime.now().plusDays(offsets[0]));
                                                        ev.setDateFin(LocalDateTime.now().plusDays(offsets[1]));
                                                        eventRepository.save(ev);
                                                });
                                        }
                                        logger.info("Event dates updated to future 2026 dates.");
                                }

                                /*
                                 * =======================
                                 * 3. CREATE EVENTS
                                 * =======================
                                 */
                                // Removed count check to allow adding new events to existing ones
                                {
                                        // Helper to find destination by name
                                        java.util.function.Function<String, Destination> findDest = name -> destinationRepository.findAll().stream()
                                                                        .filter(d -> d.getNom().contains(name)
                                                                                        || d.getType().contains(name))
                                                                        .findFirst()
                                                                        .orElse(null);
                                        Destination rabat = findDest.apply("Rabat");
                                        Destination agadir = findDest.apply("Agadir");
                                        Destination fes = findDest.apply("Fes");
                                        Destination kelaat = findDest.apply("Kelaat Mgouna");
                                        Destination tangier = findDest.apply("Tangier");
                                        Destination marrakech = findDest.apply("Marrakech");
                                        Destination sefrou = findDest.apply("Sefrou");
                                        Destination guelmim = findDest.apply("Guelmim");
                                        Destination laayoune = findDest.apply("Laayoune");
                                        Destination imilchil = findDest.apply("Imilchil");

                                        // 2. Mawazine
                                        createEvent("Mawazine", "Mawazine", "موازين", "Mawazine",
                                                        "International music festival.", "Festival international de musique.", "مهرجان موسيقي دولي.", "Festival internacional de música.",
                                                        LocalDateTime.of(2026, 6, 21, 10, 0),
                                                        LocalDateTime.of(2026, 6, 29, 23, 0), 
                                                        "Rabat", "Rabat", "الرباط", "Rabat",
                                                        rabat,
                                                        "https://www.nrjmaroc.com/sites/default/files/styles/wide/public/2024-07/crea-article-2024-07-08t101053.220.png?itok=jR0VX1zq", 
                                                        "MUSIC", "MUSIQUE", "موسيقى", "MÚSICA",
                                                        "Mawazine — Rhythms of the World is one of the world's largest music festivals, celebrating global cultural diversity in Rabat.",
                                                        "Mawazine — Rythmes du Monde est l'un des plus grands festivals de musique au monde, célébrant la diversité culturelle à Rabat.",
                                                        "موازين - إيقاعات العالم هو واحد من أكبر المهرجانات الموسيقية في العالم، يحتفي بالتنوع الثقافي في الرباط.",
                                                        "Mawazine — Ritmos del Mundo es uno de los festivales de música más grandes del mundo, que celebra la diversidad cultural en Rabat.");

                                        // 3. Timitar
                                        createEvent("Timitar", "Timitar", "تيميتار", "Timitar",
                                                        "Amazigh culture festival.", "Festival de la culture amazighe.", "مهرجان الثقافة الأمازيغية.", "Festival de la cultura amazigh.",
                                                        LocalDateTime.of(2026, 7, 5, 10, 0),
                                                        LocalDateTime.of(2026, 7, 7, 23, 0), 
                                                        "Agadir", "Agadir", "أكادير", "Agadir",
                                                        agadir,
                                                        "https://femmesdumaroc.com/wp-content/uploads/2015/10/186722735.jpg", 
                                                        "CULTURAL", "CULTUREL", "ثقافي", "CULTURAL",
                                                        "Timitar means \"signs\" in Tamazight. It celebrates Amazigh culture and its place at the heart of Moroccan identity.",
                                                        "Timitar, qui signifie \"signes\" en tamazight, célèbre la richesse de la culture amazighe à Agadir.",
                                                        "تيميتار، وتعني \"علامات\" بالأمازيغية، يحتفي بغنى الثقافة الأمازيغية في أكادير.",
                                                        "Timitar significa \"signos\" en tamazight. Celebra la cultura amazigh y su lugar en el corazón de la identidad marroquí.");

                                        // 4. Fes Sacred Music
                                        createEvent("Fes Sacred Music", "Musiques Sacrées de Fès", "فاس للموسيقى الروحية", "Música Sacra de Fez",
                                                        "Spiritual music festival.", "Festival des musiques sacrées.", "مهرجان الموسيقى الروحية.", "Festival de música espiritual.",
                                                        LocalDateTime.of(2026, 5, 17, 10, 0),
                                                        LocalDateTime.of(2026, 5, 25, 23, 0), 
                                                        "Fes", "Fès", "فاس", "Fez",
                                                        fes,
                                                        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&q=80&w=2000", 
                                                        "MUSIC", "MUSIQUE", "موسيقى", "MÚSICA",
                                                        "An intellectual and spiritual encounter aimed at dialogue between civilizations through sacred music.",
                                                        "Une rencontre intellectuelle et spirituelle visant le dialogue entre les civilisations par la musique sacrée.",
                                                        "لقاء فكري وروحي يهدف إلى الحوار بين الحضارات من خلال الموسيقى الروحية.",
                                                        "Un encuentro intelectual y espiritual destinado al diálogo entre civilizaciones a través de la música sacra.");

                                        // 5. Rose Festival
                                        createEvent("Rose Festival", "Festival des Roses", "مهرجان الورود", "Festival de las Rosas",
                                                        "Rose harvest celebration.", "Célébration de la récolte des roses.", "احتفال بموسم قطف الورود.", "Celebración de la cosecha de rosas.",
                                                        LocalDateTime.of(2026, 5, 3, 10, 0),
                                                        LocalDateTime.of(2026, 5, 6, 23, 0), 
                                                        "Kelaat Mgouna", "Kelaat Mgouna", "قلعة مكونة", "Kelaat Mgouna",
                                                        kelaat,
                                                        "https://moroccoshinydays.com/wp-content/uploads/2022/04/rose-festival-Morocco-.png", 
                                                        "TRADITIONAL", "TRADITIONNEL", "تقليدي", "TRADICIONAL",
                                                        "An annual celebration in the Dades Valley honoring the Damascus rose and its heritage.",
                                                        "Une célébration annuelle dans la Vallée du Dadès honorant la rose de Damas et son héritage.",
                                                        "احتفال سنوي في وادي دادس يحتفي بوردة دمشق وتراثها.",
                                                        "Una celebración anual en el Valle del Dades en honor a la rosa de Damasco y su herencia.");

                                        // 6. Tanjazz
                                        createEvent("Tanjazz", "Tanjazz", "طنجاز", "Tanjazz",
                                                        "Jazz festival.", "Festival de jazz.", "مهرجان الجاز.", "Festival de jazz.",
                                                        LocalDateTime.of(2026, 9, 12, 10, 0),
                                                        LocalDateTime.of(2026, 9, 14, 23, 0), 
                                                        "Tangier", "Tanger", "طنجة", "Tánger",
                                                        tangier,
                                                        "https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&q=80&w=2000", 
                                                        "MUSIC", "MUSIQUE", "موسيقى", "MÚSICA",
                                                        "Tanjazz celebrates jazz and world music in the cosmopolitan crossroads of Tangier.",
                                                        "Tanjazz célèbre le jazz et les musiques du monde à Tanger, carrefour des cultures.",
                                                        "طنجاز يحتفي بموسيقى الجاز والموسيقى العالمية في طنجة، ملتقى الثقافات.",
                                                        "Tanjazz celebra el jazz y las músicas del mundo en la encrucijada cosmopolita de Tánger.");

                                        // 7. Film Festival
                                        createEvent("Film Festival", "Festival du Film", "مهرجان الفيلم", "Festival de Cine",
                                                        "International Film Festival.", "Festival International du Film.", "المهرجان الدولي للفيلم.", "Festival Internacional de Cine.",
                                                        LocalDateTime.of(2026, 11, 24, 10, 0),
                                                        LocalDateTime.of(2026, 12, 2, 23, 0), 
                                                        "Marrakech", "Marrakech", "مراكش", "Marrakech",
                                                        marrakech,
                                                        "https://www.hrw.org/sites/default/files/styles/embed_xxl/public/media_2024/03/202403film_fest_presser.JPG?itok=94ARRmtp", 
                                                        "FESTIVAL", "FESTIVAL", "مهرجان", "FESTIVAL",
                                                        "One of the major events dedicated to the 7th art in Africa and the Arab world.",
                                                        "L'un des événements majeurs dédiés au 7ème art en Afrique et dans le monde arabe.",
                                                        "واحد من الأحداث الكبرى المخصصة للفن السابع في أفريقيا والعالم العربي.",
                                                        "Uno de los grandes eventos dedicados al séptimo arte en África y el mundo árabe.");

                                        // 8. Cherry Festival
                                        createEvent("Cherry Festival", "Festival des Cerises", "مهرجان حب الملوك", "Festival de las Cerezas",
                                                        "Cherry harvest festival.", "Fête de la cerise.", "موسم حب الملوك.", "Fiesta de la cereza.",
                                                        LocalDateTime.of(2026, 6, 7, 10, 0),
                                                        LocalDateTime.of(2026, 6, 9, 23, 0), 
                                                        "Sefrou", "Sefrou", "صفرو", "Sefrou",
                                                        sefrou,
                                                        "https://www.awesomemitten.com/wp-content/uploads/2015/07/Cherry-Festival.png", 
                                                        "FESTIVAL", "FESTIVAL", "مهرجان", "FESTIVAL",
                                                        "The oldest cherry harvest celebration in Morocco, recognized by UNESCO.",
                                                        "La plus ancienne fête de récolte des cerises au Maroc, classée par l'UNESCO.",
                                                        "أقدم احتفال بموسم قطف حب الملوك في المغرب، مصنف من طرف اليونسكو.",
                                                        "La celebración más antigua de la cosecha de cerezas en Marruecos, reconocida por la UNESCO.");

                                        // 9. Camel Festival
                                        createEvent("Camel Festival", "Festival du Chameau", "مهرجان الإبل", "Festival del Camello",
                                                        "Nomadic heritage celebration.", "Célébration de l'héritage nomade.", "احتفال بالتراث البدوي.", "Celebración del patrimonio nómada.",
                                                        LocalDateTime.of(2026, 8, 10, 10, 0),
                                                        LocalDateTime.of(2026, 8, 13, 23, 0), 
                                                        "Guelmim", "Guelmim", "كلميم", "Guelmim",
                                                        guelmim,
                                                        "https://images.hindustantimes.com/rf/image_size_960x540/HT/p2/2018/01/14/Pictures/_c52e8e54-f8e2-11e7-9cc5-99c3d5c09a90.jpg", 
                                                        "TRADITIONAL", "TRADITIONNEL", "تقليدي", "TRADICIONAL",
                                                        "A gathering of Saharan nomadic tribes celebrating camel culture.",
                                                        "Un rassemblement de tribus nomades sahariennes célébrant la culture du chameau.",
                                                        "تجمع للقبائل البدوية الصحراوية يحتفي بثقافة الإبل.",
                                                        "Una reunión de tribus nómadas del Sahara celebrando la cultura del camello.");

                                        // 10. Sahara Marathon
                                        createEvent("Sahara Marathon", "Marathon du Sahara", "ماراثون الصحراء", "Maratón del Sahara",
                                                        "Desert marathon race.", "Course de marathon dans le désert.", "سباق ماراثون الصحراء.", "Carrera de maratón en el desierto.",
                                                        LocalDateTime.of(2026, 2, 20, 10, 0),
                                                        LocalDateTime.of(2026, 2, 22, 23, 0), 
                                                        "Laayoune", "Laâyoune", "العيون", "El Aaiún",
                                                        laayoune,
                                                        "https://www.moroccoworldnews.com/wp-content/uploads/2021/02/aym_4243.jpg",
                                                        "SPORT", "SPORT", "رياضة", "DEPORTE",
                                                        "An international sports challenge in the unique setting of the Sahara desert.",
                                                        "Un défi sportif international dans le cadre unique du désert du Sahara.",
                                                        "تحدي رياضي دولي في الإطار الفريد لصحراء العيون.",
                                                        "Un desafío deportivo internacional en el entorno único del desierto del Sahara.");


                                        // 11. Imilchil Marriage
                                        createEvent("Imilchil Marriage", "Moussem d'Imilchil", "موسم إميلشيل للخطوبة", "Festival de Imilchil",
                                                        "Traditional engagement festival.", "Festival traditionnel des fiançailles.", "مهرجان الخطوبة التقليدي.", "Festival tradicional de compromiso.",
                                                        LocalDateTime.of(2026, 9, 15, 10, 0),
                                                        LocalDateTime.of(2026, 9, 17, 23, 0), 
                                                        "Imilchil", "Imilchil", "إميلشيل", "Imilchil",
                                                        imilchil,
                                                        "https://www.moroccoworldnews.com/wp-content/uploads/2018/09/imilchil.jpg", 
                                                        "TRADITIONAL", "TRADITIONNEL", "تقليدي", "TRADICIONAL",
                                                        "The legendary engagement festival in the High Atlas mountains, rooted in the story of Tislit and Isli.",
                                                        "Le légendaire moussem des fiançailles dans le Haut Atlas, ancré dans l'histoire de Tislit et Isli.",
                                                        "موسم الخطوبة الأسطوري في جبال الأطلس الكبير، المتجذر في قصة تيسليت وإيسلي.",
                                                        "El legendario festival de compromiso en las montañas del Alto Atlas, arraigado en la historia de Tislit e Isli.");

                                        // 12. Tan-Tan Moussem
                                        createEvent("Tan-Tan Moussem", "Moussem de Tan-Tan", "موسم طانطان", "Moussem de Tan-Tan",
                                                        "UNESCO recognized nomadic heritage.", "Patrimoine nomade reconnu par l'UNESCO.", "تراث بدوي معترف به من طرف اليونسكو.", "Patrimonio nómada reconocido por la UNESCO.",
                                                        LocalDateTime.of(2026, 6, 12, 10, 0),
                                                        LocalDateTime.of(2026, 6, 18, 23, 0), 
                                                        "Tan-Tan", "Tan-Tan", "طانطان", "Tan-Tan",
                                                        guelmim,
                                                        "https://aujourdhui.ma/wp-content/uploads/2017/05/Moussem-de-Tan-Tan.jpg", 
                                                        "TRADITIONAL", "TRADITIONNEL", "تقليدي", "TRADICIONAL",
                                                        "One of the largest nomadic gatherings in the world, celebrating Saharan culture.",
                                                        "L'un des plus grands rassemblements nomades au monde, célébrant la culture saharienne.",
                                                        "واحد من أكبر التجمعات البدوية في العالم، يحتفي بالثقافة الصحراوية.",
                                                        "Una de las reuniones nómadas más grandes del mundo, que celebra la cultura saharaui.");

                                        // 13. Ifrane Cedar Festival
                                        Destination ifrane = findDest.apply("Azrou"); // Azrou has the forest
                                        createEvent("Cedar Festival", "Festival du Cèdre", "مهرجان الأرز", "Festival del Cedro",
                                                        "Celebrating nature in the Middle Atlas.", "Célébration de la nature du Moyen Atlas.", "الاحتفاء بطبيعة الأطلس المتوسط.", "Celebrando la naturaleza en el Atlas Medio.",
                                                        LocalDateTime.of(2026, 7, 10, 10, 0),
                                                        LocalDateTime.of(2026, 7, 12, 23, 0), 
                                                        "Ifrane", "Ifrane", "إفران", "Ifrane",
                                                        ifrane,
                                                        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&q=80&w=2000", 
                                                        "FESTIVAL", "FESTIVAL", "مهرجان", "FESTIVAL",
                                                        "Celebrating the cedar forests and biodiversity of the Middle Atlas mountains.",
                                                        "Célébration des forêts de cèdres et de la biodiversité du Moyen Atlas.",
                                                        "الاحتفاء بغابات الأرز والتنوع البيولوجي في جبال الأطلس المتوسط.",
                                                        "Celebrando los bosques de cedros y la biodiversidad de las montañas del Atlas Medio.");

                                        // 14. Dakhla Kitesurf World Cup
                                        createEvent("Kitesurf World Cup", "Coupe du Monde de Kitesurf", "كأس العالم لركوب الأمواج", "Copa del Mundo de Kitesurf",
                                                        "International kitesurfing competition.", "Compétition internationale de kitesurf.", "مسابقة دولية لركوب الأمواج.", "Competición internacional de kitesurf.",
                                                        LocalDateTime.of(2026, 10, 15, 10, 0),
                                                        LocalDateTime.of(2026, 10, 20, 23, 0), 
                                                        "Dakhla Lagoon", "Lagune de Dakhla", "داخلة لاغون", "Laguna de Dajla",
                                                        findDest.apply("Dakhla"),
                                                        "https://www.planetkitesurfholidays.com/blog/wp-content/uploads/sites/2/2017/05/spain-tarifa-kitesurf-worldtour.jpg", 
                                                        "SPORT", "SPORT", "رياضة", "DEPORTE",
                                                        "World-class kitesurfing competition in the spectacular Dakhla lagoon.",
                                                        "Compétition mondiale de kitesurf dans la spectaculaire lagune de Dakhla.",
                                                        "مسابقة عالمية لركوب الأمواج في بحيرة الداخلة المذهلة.",
                                                        "Competición mundial de kitesurf en la espectacular laguna de Dajla.");

                                        // 15. Meknes International Animation Film Festival (FICAM)
                                        createEvent("FICAM Meknes", "FICAM Meknès", "فيكام مكناس", "FICAM Meknes",
                                                        "Animation film festival.", "Festival du film d'animation.", "مهرجان سينما التحريك.", "Festival de cine de animación.",
                                                        LocalDateTime.of(2026, 5, 10, 10, 0),
                                                        LocalDateTime.of(2026, 5, 15, 23, 0), 
                                                        "Meknes", "Meknès", "مكناس", "Mequinez",
                                                        findDest.apply("Meknes"),
                                                        "https://www.mapmeknes.ma/wp-content/uploads/2019/10/FICAM-2.jpg", 
                                                        "FESTIVAL", "FESTIVAL", "مهرجان", "FESTIVAL",
                                                        "International festival dedicated to animated films in the historic city of Meknes.",
                                                        "Festival international dédié au film d'animation dans la ville historique de Meknès.",
                                                        "مهرجان دولي مخصص لأفلام التحريك في مدينة مكناس التاريخية.",
                                                        "Festival internacional dedicado al cine de animación en la histórica ciudad de Mequinez.");
                                }

                                /*
                                 * =======================
                                 * 4. CREATE OFFERS
                                 * =======================
                                 */
                                if (offerRepository.count() == 0) {
                                        List<Destination> allDestinations = destinationRepository.findAll();
                                        List<Offer> initialOffers = new ArrayList<>();
                                        for (Destination d : allDestinations) {
                                                // 1 Hotel
                                                initialOffers.add(Offer.builder()
                                                                .nameEn(d.getNomEn() + " Royal Resort")
                                                                .nameFr(d.getNomFr() + " Royal Resort")
                                                                .nameAr("منتجع " + d.getNomAr() + " الملكي")
                                                                .nameEs(d.getNomEs() + " Royal Resort")
                                                                .descriptionEn("Premium luxury stay in the heart of " + d.getNomEn())
                                                                .descriptionFr("Séjour de luxe au cœur de " + d.getNomFr())
                                                                .descriptionAr("إقامة فاخرة في قلب " + d.getNomAr())
                                                                .descriptionEs("Estancia de lujo en el corazón de " + d.getNomEs())
                                                                .type(OfferType.HOTEL)
                                                                .destination(d)
                                                                .stars(5)
                                                                .roomTypeEn("Luxury Suite")
                                                                .roomTypeFr("Suite de Luxe")
                                                                .roomTypeAr("جناح فاخر")
                                                                .roomTypeEs("Suite de Lujo")
                                                                .pricePerNight(1850.0)
                                                                .build());

                                                // 1 Restaurant
                                                initialOffers.add(Offer.builder()
                                                                .nameEn(d.getNomEn() + " Traditional Kitchen")
                                                                .nameFr("Cuisine Traditionnelle de " + d.getNomFr())
                                                                .nameAr("مطبخ " + d.getNomAr() + " التقليدي")
                                                                .nameEs("Cocina Tradicional de " + d.getNomEs())
                                                                .descriptionEn("Authentic local flavors and traditional recipes.")
                                                                .descriptionFr("Saveurs locales authentiques et recettes traditionnelles.")
                                                                .descriptionAr("نكهات محلية أصيلة ووصفات تقليدية.")
                                                                .descriptionEs("Sabores locales auténticos y tradiciones culinarias.")
                                                                .type(OfferType.RESTAURANT)
                                                                .destination(d)
                                                                .cuisineTypeEn("Moroccan Traditional")
                                                                .cuisineTypeFr("Marocaine Traditionnelle")
                                                                .cuisineTypeAr("مغربي تقليدي")
                                                                .cuisineTypeEs("Marroquí Tradicional")
                                                                .averagePrice(350.0)
                                                                .build());

                                                // 1 Activity
                                                initialOffers.add(Offer.builder()
                                                                .nameEn("Discovery Tour: " + d.getNomEn())
                                                                .nameFr("Visite Découverte : " + d.getNomFr())
                                                                .nameAr("جولة اكتشاف: " + d.getNomAr())
                                                                .nameEs("Tour de Descubrimiento: " + d.getNomEs())
                                                                .descriptionEn("A guided journey through the most iconic spots.")
                                                                .descriptionFr("Un voyage guidé à travers les lieux les plus emblématiques.")
                                                                .descriptionAr("رحلة بصحبة مرشد عبر الأماكن الأكثر شهرة.")
                                                                .descriptionEs("Un viaje guiado por los lugares más emblemáticos.")
                                                                .type(OfferType.ACTIVITY)
                                                                .destination(d)
                                                                .durationEn("4 Hours")
                                                                .durationFr("4 Heures")
                                                                .durationAr("4 ساعات")
                                                                .durationEs("4 Horas")
                                                                .activityTypeEn("Cultural Tour")
                                                                .activityTypeFr("Visite Culturelle")
                                                                .activityTypeAr("جولة ثقافية")
                                                                .activityTypeEs("Tour Cultural")
                                                                .price(450.0)
                                                                .build());
                                        }
                                        offerRepository.saveAll(initialOffers);
                                        logger.info("Generated {} default Offers.", initialOffers.size());
                                }

                                logger.info("Data Initialization Completed Successfully");

                        } catch (Exception e) {
                                logger.error("Error during data initialization", e);
                        }
                };

        }

        private Destination createDestination(
                        String nomEn, String nomFr, String nomAr, String nomEs,
                        String descEn, String descFr, String descAr, String descEs,
                        String typeEn, String typeFr, String typeAr, String typeEs,
                        String category, Double lat, Double lon, String imageUrl,
                        String hEn, String hFr, String hAr, String hEs,
                        String bTEn, String bTFr, String bTAr, String bTEs,
                        String lEn, String lFr, String lAr, String lEs,
                        Double averageCost, Long viewCount) {
                Destination existing = destinationRepository.findByNomEn(nomEn).orElse(null);
                if (existing != null) {
                        existing.setNomFr(nomFr); existing.setNomAr(nomAr); existing.setNomEs(nomEs);
                        existing.setDescriptionEn(descEn); existing.setDescriptionFr(descFr); existing.setDescriptionAr(descAr); existing.setDescriptionEs(descEs);
                        existing.setHistoricalDescriptionEn(hEn); existing.setHistoricalDescriptionFr(hFr); existing.setHistoricalDescriptionAr(hAr); existing.setHistoricalDescriptionEs(hEs);
                        existing.setBestTimeEn(bTEn); existing.setBestTimeFr(bTFr); existing.setBestTimeAr(bTAr); existing.setBestTimeEs(bTEs);
                        existing.setLanguagesEn(lEn); existing.setLanguagesFr(lFr); existing.setLanguagesAr(lAr); existing.setLanguagesEs(lEs);
                        existing.setAverageCost(averageCost);
                        existing.setViewCount(viewCount != null ? viewCount : existing.getViewCount());
                        existing.setTypeEn(typeEn); existing.setTypeFr(typeFr); existing.setTypeAr(typeAr); existing.setTypeEs(typeEs);
                        destinationRepository.save(existing);
                        return null; 
                }
                Destination d = new Destination();
                d.setNomEn(nomEn); d.setNomFr(nomFr); d.setNomAr(nomAr); d.setNomEs(nomEs);
                d.setDescriptionEn(descEn); d.setDescriptionFr(descFr); d.setDescriptionAr(descAr); d.setDescriptionEs(descEs);
                d.setHistoricalDescriptionEn(hEn); d.setHistoricalDescriptionFr(hFr); d.setHistoricalDescriptionAr(hAr); d.setHistoricalDescriptionEs(hEs);
                d.setBestTimeEn(bTEn); d.setBestTimeFr(bTFr); d.setBestTimeAr(bTAr); d.setBestTimeEs(bTEs);
                d.setLanguagesEn(lEn); d.setLanguagesFr(lFr); d.setLanguagesAr(lAr); d.setLanguagesEs(lEs);
                d.setAverageCost(averageCost);
                d.setViewCount(viewCount != null ? viewCount : 0L);
                d.setTypeEn(typeEn); d.setTypeFr(typeFr); d.setTypeAr(typeAr); d.setTypeEs(typeEs);
                d.setCategorie(category);
                d.setLatitude(lat);
                d.setLongitude(lon);

                if (imageUrl != null) {
                        Media m = Media.builder()
                                        .url(imageUrl)
                                        .type("IMAGE")
                                        .destination(d)
                                        .build();
                        d.getMedias().add(m);
                }
                return d;
        }

        private void createEvent(String nameEn, String nameFr, String nameAr, String nameEs,
                        String descEn, String descFr, String descAr, String descEs,
                        LocalDateTime start, LocalDateTime end, 
                        String lieuEn, String lieuFr, String lieuAr, String lieuEs,
                        Destination dest, String imgUrl, 
                        String typeEn, String typeFr, String typeAr, String typeEs,
                        String histEn, String histFr, String histAr, String histEs) {
                EvenementCulturel e = eventRepository.findByNomEn(nameEn).orElse(EvenementCulturel.builder().build());

                e.setNomEn(nameEn); e.setNomFr(nameFr); e.setNomAr(nameAr); e.setNomEs(nameEs);
                e.setDescriptionEn(descEn); e.setDescriptionFr(descFr); e.setDescriptionAr(descAr); e.setDescriptionEs(descEs);
                e.setDateDebut(start);
                e.setDateFin(end);
                e.setLieuEn(lieuEn); e.setLieuFr(lieuFr); e.setLieuAr(lieuAr); e.setLieuEs(lieuEs);
                e.setDestination(dest);
                e.setEventTypeEn(typeEn); e.setEventTypeFr(typeFr); e.setEventTypeAr(typeAr); e.setEventTypeEs(typeEs);
                e.setImageUrl(imgUrl);
                e.setHistoriqueEn(histEn); e.setHistoriqueFr(histFr); e.setHistoriqueAr(histAr); e.setHistoriqueEs(histEs);

                eventRepository.save(e);
        }
}
