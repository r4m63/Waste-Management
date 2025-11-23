import { useState } from 'react'
import YandexMap from '../components/YandexMap'
import styles from './home.module.css'

export default function Home() {
    const [openFaq, setOpenFaq] = useState(null)

    const faqData = [
        {
            id: 1,
            question: 'Какие виды отходов вы принимаете?',
            answer: 'Мы принимаем широкий спектр отходов, включая пластик, стекло, металл, бумагу, картон, бытовую технику, электронику и опасные отходы. Каждый вид отходов обрабатывается согласно экологическим стандартам.'
        },
        {
            id: 2,
            question: 'Сколько стоят ваши услуги?',
            answer: 'Стоимость услуг зависит от типа и объема отходов. Для частных лиц у нас действуют специальные тарифы, для предприятий предусмотрены корпоративные программы. Свяжитесь с нами для получения персонального предложения.'
        },
        {
            id: 3,
            question: 'Какие документы нужны для сдачи отходов?',
            answer: 'Для частных лиц документы не требуются. Для юридических лиц необходимо предоставить паспорт отходов и документы, подтверждающие право собственности на отходы. Наши консультанты помогут вам подготовить необходимые документы.'
        },
        {
            id: 4,
            question: 'Как часто вы можете забирать отходы?',
            answer: 'Частота вывоза зависит от типа клиента и объема отходов. Мы предлагаем разовые вывозы, регулярное обслуживание (ежедневно, еженедельно, ежемесячно) и вызов по требованию. Гибкий график позволяет выбрать оптимальный вариант.'
        },
        {
            id: 5,
            question: 'Что происходит с отходами после сдачи?',
            answer: 'Все отходы проходят сортировку на наших объектах, после чего направляются на специализированные предприятия для переработки, утилизации или безопасного захоронения. Мы гарантируем полное соответствие экологическим нормам.'
        },
        {
            id: 6,
            question: 'Работаете ли вы с предприятиями?',
            answer: 'Да, мы предоставляем комплексные услуги для предприятий любого масштаба. Это включает регулярный вывоз отходов, консультации по экологическому законодательству, разработку программ по сокращению отходов и ведение экологической отчетности.'
        }
    ]

    const toggleFaq = (id) => {
        setOpenFaq(openFaq === id ? null : id)
    }
    return (
        <div>
            {/* Top Banner */}
            <div className={styles.topBanner}>
                <div className={styles.container}>
                    <span>2024 • Ранняя регистрация открыта {'>'}</span>
                    <button className={styles.closeBanner}>✕</button>
                </div>
            </div>

            {/* Header */}
            <header className={styles.header}>
                <div className={styles.container}>
                    <nav className={styles.nav}>
                        <div className={styles.logo}>Waste Management</div>
                        <ul className={styles.navLinks}>
                            <li><a href="#features">Особенности</a></li>
                            <li><a href="#accounts">Аккаунты</a></li>
                            <li><a href="#company">Компания</a></li>
                            <li><a href="#insight">Инсайты</a></li>
                        </ul>
                        <div className={styles.headerActions}>
                            <a href="#login" className={styles.loginLink}>Войти</a>
                            <button className={styles.signUpBtn}>Регистрация {'>'}</button>
                        </div>
                    </nav>
                </div>
            </header>

            {/* Hero секция */}
            <section className={styles.hero}>
                <div className={styles.container}>
                    <div className={styles.heroGrid}>
                        {/* Левая колонка - текст */}
                        <div className={styles.heroLeft}>
                            <div className={styles.preHeadline}>ПОПРОБУЙТЕ СЕЙЧАС!</div>
                            <h1 className={styles.heroTitle}>
                                Измените способ управления вашими <em>отходами</em>
                            </h1>
                            <p className={styles.heroDescription}>
                                От ежедневного сбора до планирования будущего с переработкой и утилизацией, 
                                Waste Management помогает вам получить больше от ваших отходов.
                            </p>
                            <button className={styles.btnPrimary}>Начать сейчас</button>
                            <div className={styles.heroRating}>
                                <span className={styles.ratingValue}>5.0</span>
                                <div className={styles.stars}>
                                    <span>⭐</span>
                                    <span>⭐</span>
                                    <span>⭐</span>
                                    <span>⭐</span>
                                    <span>⭐</span>
                                </div>
                                <span className={styles.ratingText}>из 120+ отзывов</span>
                            </div>
                        </div>

                        {/* Правая колонка - сетка */}
                        <div className={styles.heroRight}>
                            <div className={styles.heroGridRight}>
                                {/* Top Left - изображение */}
                                <div className={styles.gridBlockImage}>
                                    <div className={styles.phoneMockup}>📱</div>
                                    <div className={styles.decorShape1}></div>
                                    <div className={styles.decorShape2}></div>
                                </div>

                                {/* Top Right - валюта/статистика */}
                                <div className={styles.gridBlockStats1}>
                                    <div className={styles.statNumber}>50+</div>
                                    <div className={styles.statLabel}>Точек приема</div>
                                    <div className={styles.globeIcon}>🌍</div>
                                </div>

                                {/* Bottom Left - активные пользователи */}
                                <div className={styles.gridBlockStats2}>
                                    <div className={styles.starIcons}>
                                        <span>✨</span>
                                        <span>✨</span>
                                    </div>
                                    <div className={styles.statLabel}>Активные пользователи</div>
                                    <div className={styles.userAvatars}>
                                        <span className={styles.avatar}>👤</span>
                                        <span className={styles.avatar}>👤</span>
                                        <span className={styles.avatar}>👤</span>
                                        <span className={styles.moreUsers}>→</span>
                                    </div>
                                </div>

                                {/* Bottom Right - сохранения */}
                                <div className={styles.gridBlockStats3}>
                                    <div className={styles.savingAmount}>
                                        <span>196,000</span>
                                        <span className={styles.arrowUp}>↑</span>
                                    </div>
                                    <div className={styles.chart}>
                                        <div className={styles.chartBar} style={{height: '40%'}}></div>
                                        <div className={styles.chartBar} style={{height: '60%'}}></div>
                                        <div className={styles.chartBar} style={{height: '80%'}}></div>
                                        <div className={styles.chartBar} style={{height: '100%'}}></div>
                                    </div>
                                    <div className={styles.statLabelWhite}>Переработка</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Логотипы компаний */}
            <section className={styles.logosSection}>
                <div className={styles.container}>
                    <div className={styles.logosContainer}>
                        <div className={styles.logoItem}>
                            <span className={styles.logoIcon}>☀️</span>
                            <span className={styles.logoText}>EcoLoop</span>
                        </div>
                        <div className={styles.logoItem}>
                            <span className={styles.logoIcon}>🔗</span>
                            <span className={styles.logoText}>GreenSpot</span>
                        </div>
                        <div className={styles.logoItem}>
                            <span className={styles.logoIcon}>🧠</span>
                            <span className={styles.logoText}>EcoMind</span>
                        </div>
                        <div className={styles.logoItem}>
                            <span className={styles.logoIcon}>⚡</span>
                            <span className={styles.logoText}>GreenCast</span>
                        </div>
                        <div className={styles.logoItem}>
                            <span className={styles.logoIcon}>✨</span>
                            <span className={styles.logoText}>ZenWaste</span>
                        </div>
                    </div>
                </div>
            </section>

            {/* О компании */}
            <section className={styles.about}>
                <div className={styles.container}>
                    <h2 className={styles.sectionTitle}>О компании</h2>
                    <div className={styles.aboutContent}>
                        <div className={styles.aboutText}>
                            <p>
                                <strong>Waste Management</strong> — ведущая компания в области сбора, 
                                транспортировки и утилизации отходов. Мы работаем по всей стране, 
                                предоставляя качественные услуги частным лицам и предприятиям.
                            </p>
                            <p>
                                Наша миссия — сделать процесс утилизации отходов максимально удобным 
                                и экологически безопасным. Мы используем современные технологии и 
                                оборудование для эффективной переработки различных видов мусора.
                            </p>
                            <p>
                                За годы работы мы зарекомендовали себя как надежный партнер, 
                                который ценит каждого клиента и стремится к постоянному улучшению 
                                качества услуг.
                            </p>
                        </div>
                        <div className={styles.aboutStats}>
                            <div className={styles.statItem}>
                                <div className={styles.statNumber}>50+</div>
                                <div className={styles.statLabel}>Точек приема</div>
                            </div>
                            <div className={styles.statItem}>
                                <div className={styles.statNumber}>10000+</div>
                                <div className={styles.statLabel}>Довольных клиентов</div>
                            </div>
                            <div className={styles.statItem}>
                                <div className={styles.statNumber}>8</div>
                                <div className={styles.statLabel}>Лет опыта</div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Кто мы */}
            <section className={styles.whoWeAre}>
                <div className={styles.container}>
                    <h2 className={styles.sectionTitle}>Кто мы</h2>
                    <div className={styles.teamGrid}>
                        <div className={styles.teamCard}>
                            <div className={styles.teamIcon}>👥</div>
                            <h3>Команда профессионалов</h3>
                            <p>
                                У нас работают опытные специалисты, которые знают все тонкости 
                                работы с отходами. Каждый сотрудник проходит специальное обучение 
                                и регулярно повышает квалификацию.
                            </p>
                        </div>
                        <div className={styles.teamCard}>
                            <div className={styles.teamIcon}>🌱</div>
                            <h3>Экологически ответственные</h3>
                            <p>
                                Мы понимаем важность заботы об окружающей среде. Все наши процессы 
                                направлены на минимизацию негативного воздействия на природу и 
                                максимальную переработку отходов.
                            </p>
                        </div>
                        <div className={styles.teamCard}>
                            <div className={styles.teamIcon}>🚛</div>
                            <h3>Современный парк техники</h3>
                            <p>
                                Наш автопарк регулярно обновляется новым оборудованием. 
                                Мы используем только проверенные технологии для эффективной 
                                работы с различными видами отходов.
                            </p>
                        </div>
                        <div className={styles.teamCard}>
                            <div className={styles.teamIcon}>💚</div>
                            <h3>Социальная ответственность</h3>
                            <p>
                                Мы активно участвуем в экологических программах и поддерживаем 
                                инициативы по улучшению экологической ситуации в регионах нашей работы.
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Преимущества */}
            <section className={styles.advantages}>
                <div className={styles.container}>
                    <h2 className={styles.sectionTitle}>Преимущества работы с нами</h2>
                    <div className={styles.advantagesGrid}>
                        <div className={styles.advantageItem}>
                            <div className={styles.advantageNumber}>01</div>
                            <h3>Удобное расположение</h3>
                            <p>
                                Более 50 точек приема по всему городу. 
                                Найдите ближайшую точку на нашей карте.
                            </p>
                        </div>
                        <div className={styles.advantageItem}>
                            <div className={styles.advantageNumber}>02</div>
                            <h3>Круглосуточный режим</h3>
                            <p>
                                Большинство наших точек работают 24/7. 
                                Приезжайте в удобное для вас время.
                            </p>
                        </div>
                        <div className={styles.advantageItem}>
                            <div className={styles.advantageNumber}>03</div>
                            <h3>Разные виды отходов</h3>
                            <p>
                                Принимаем пластик, стекло, металл, бумагу, 
                                бытовую технику и другие виды отходов.
                            </p>
                        </div>
                        <div className={styles.advantageItem}>
                            <div className={styles.advantageNumber}>04</div>
                            <h3>Выгодные условия</h3>
                            <p>
                                Программы лояльности и специальные предложения 
                                для постоянных клиентов.
                            </p>
                        </div>
                        <div className={styles.advantageItem}>
                            <div className={styles.advantageNumber}>05</div>
                            <h3>Быстрое обслуживание</h3>
                            <p>
                                Минимум времени на оформление. 
                                Вежливый персонал и оперативная работа.
                            </p>
                        </div>
                        <div className={styles.advantageItem}>
                            <div className={styles.advantageNumber}>06</div>
                            <h3>Экологическая переработка</h3>
                            <p>
                                Все отходы направляются на специализированные 
                                предприятия для безопасной переработки.
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Карта с точками приема */}
            <section className={styles.mapSection}>
                <div className={styles.container}>
                    <h2 className={styles.sectionTitle}>Наши точки приема</h2>
                    <p className={styles.mapDescription}>
                        Найдите ближайшую точку приема мусора на интерактивной карте. 
                        Все точки работают круглосуточно и принимают различные виды отходов.
                    </p>
                    <div className={styles.mapContainer}>
                        <YandexMap />
                    </div>
                </div>
            </section>

            {/* FAQ секция */}
            <section className={styles.faqSection}>
                <div className={styles.container}>
                    <h2 className={styles.sectionTitle}>Часто задаваемые вопросы</h2>
                    <div className={styles.faqContainer}>
                        {faqData.map((faq) => (
                            <div key={faq.id} className={styles.faqItem}>
                                <button 
                                    className={`${styles.faqQuestion} ${openFaq === faq.id ? styles.faqQuestionOpen : ''}`}
                                    onClick={() => toggleFaq(faq.id)}
                                >
                                    <span>{faq.question}</span>
                                    <span className={styles.faqIcon}>{openFaq === faq.id ? '−' : '+'}</span>
                                </button>
                                {openFaq === faq.id && (
                                    <div className={styles.faqAnswer}>
                                        <p>{faq.answer}</p>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className={styles.footer}>
                <div className={styles.container}>
                    <div className={styles.footerContent}>
                        <div className={styles.footerNav}>
                            <div className={styles.footerColumn}>
                                <h4 className={styles.footerColumnTitle}>Услуги</h4>
                                <ul className={styles.footerLinks}>
                                    <li><a href="#saving">Переработка</a></li>
                                    <li><a href="#join">Прием отходов</a></li>
                                    <li><a href="#crypto">Эко-проекты</a></li>
                                    <li><a href="#freelance">Консультации</a></li>
                                    <li><a href="#commodities">Партнерство</a></li>
                                </ul>
                            </div>
                            <div className={styles.footerColumn}>
                                <h4 className={styles.footerColumnTitle}>Помощь</h4>
                                <ul className={styles.footerLinks}>
                                    <li><a href="#help">Поддержка клиентов</a></li>
                                    <li><a href="#community">Сообщество</a></li>
                                    <li><a href="#blog">Блог</a></li>
                                </ul>
                            </div>
                            <div className={styles.footerColumn}>
                                <h4 className={styles.footerColumnTitle}>Ресурсы</h4>
                                <ul className={styles.footerLinks}>
                                    <li><a href="#cards">Карта точек</a></li>
                                    <li><a href="#accounts">Личный кабинет</a></li>
                                    <li><a href="#payment">Оплата</a></li>
                                </ul>
                            </div>
                            <div className={styles.footerColumn}>
                                <h4 className={styles.footerColumnTitle}>Компания</h4>
                                <ul className={styles.footerLinks}>
                                    <li><a href="#about">О нас</a></li>
                                    <li><a href="#contact">Контакты</a></li>
                                    <li><a href="#sustainability">Экология</a></li>
                                    <li><a href="#career">Карьера</a></li>
                                </ul>
                            </div>
                        </div>
                        <div className={styles.footerBrand}>
                            <div className={styles.footerLogo}>Waste Management</div>
                            <div className={styles.footerAddress}>
                                181 Bay Street<br />
                                Bay Wellington Tower, Suite 292<br />
                                Toronto, Ontario<br />
                                M5J 2T3
                            </div>
                            <div className={styles.footerLanguage}>
                                <span className={styles.flagIcon}>🇷🇺</span>
                                <span>Русский (RU)</span>
                            </div>
                        </div>
                    </div>
                    <div className={styles.footerBottom}>
                        <div className={styles.footerCopyright}>
                            &copy; Waste Management Ltd 2024
                        </div>
                        <div className={styles.footerLegal}>
                            <a href="#privacy">Политика конфиденциальности</a>
                            <a href="#terms">Условия использования</a>
                            <a href="#disclosure">Раскрытие информации</a>
                        </div>
                    </div>
                </div>
            </footer>
        </div>
    )
}
