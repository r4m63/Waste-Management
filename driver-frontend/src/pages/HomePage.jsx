import { Link } from 'react-router-dom'

function HomePage() {
  return (
    <section className="panel">
      <header className="panel__header">
        <p className="eyebrow">Добро пожаловать</p>
        <h1 className="panel__title">Рабочая панель водителя</h1>
      </header>
      <p className="panel__body">
        Здесь будут собраны ключевые сценарии для водителя: маршрут на сегодня, задачи и быстрый доступ
        к профилю. Сейчас это стартовая страница, чтобы убедиться, что роутер настроен и работает.
      </p>
      <div className="panel__actions">
        <Link to="/routes" className="button">
          Перейти к маршрутам
        </Link>
        <Link to="/profile" className="button button--ghost">
          Открыть профиль
        </Link>
      </div>
    </section>
  )
}

export default HomePage
