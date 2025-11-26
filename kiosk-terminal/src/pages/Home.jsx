import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Chip,
  Divider,
} from '@nextui-org/react'

export default function Home() {
  return (
    <section className="flex min-h-[calc(100vh-4rem)] flex-col items-center justify-center gap-8 px-6 py-12">
      <div className="text-center space-y-3">
        <p className="text-sm font-medium uppercase tracking-[0.3em] text-default-500">
          Waste Management
        </p>
        <h1 className="text-4xl font-semibold text-foreground md:text-5xl">
          Киоск-терминал
        </h1>
        <p className="mx-auto max-w-2xl text-base text-default-500 md:text-lg">
          Управляйте очередью вывоза отходов и отслеживайте статусы контейнеров
          прямо с терминала самообслуживания.
        </p>
      </div>

      <Card className="w-full max-w-lg bg-content1/80 shadow-large backdrop-blur">
        <CardHeader className="flex flex-col items-start gap-2">
          <Chip color="success" variant="flat">
            Онлайн
          </Chip>
          <div>
            <p className="text-small text-default-500">Статус системы</p>
            <h2 className="text-2xl font-semibold">Готов к работе</h2>
          </div>
        </CardHeader>
        <Divider />
        <CardBody className="space-y-4">
          <p className="text-default-500">
            Вы можете начать новую сессию обслуживания или проверить активные
            заявки. Компоненты NextUI уже подключены и готовы к использованию.
          </p>
        </CardBody>
        <CardFooter className="flex flex-wrap gap-3">
          <Button color="primary" className="flex-1 min-w-[140px]">
            Новая сессия
          </Button>
          <Button variant="bordered" color="secondary" className="flex-1 min-w-[140px]">
            Активные заявки
          </Button>
        </CardFooter>
      </Card>
    </section>
  )
}
