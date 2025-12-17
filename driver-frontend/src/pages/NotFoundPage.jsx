import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <section className="panel panel--centered">
      <p className="eyebrow">404</p>
      <h1 className="panel__title">Страница не найдена</h1>
      <p className="panel__body">Маршрут не существует. Вернитесь на главную.</p>
      <Link to="/" className="button">
        На главную
      </Link>
    </section>
  )
}

export default NotFoundPage
