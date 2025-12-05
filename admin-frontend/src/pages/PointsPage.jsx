// pages/PointsPage.jsx

export default function PointsPage() {
    return (
        <div className="flex flex-1 flex-col gap-4">
            <div className="flex flex-col gap-2">
                <h1 className="text-2xl font-semibold leading-none tracking-tight">Точки</h1>
                <p className="text-sm text-muted-foreground">
                    Список точек сбора с быстрым предпросмотром карты и статусов.
                </p>
            </div>

            <div className="grid auto-rows-min gap-4 md:grid-cols-3">
                <div className="bg-muted/50 aspect-video rounded-xl"/>
                <div className="bg-muted/50 aspect-video rounded-xl"/>
                <div className="bg-muted/50 aspect-video rounded-xl"/>
            </div>

            <div className="bg-muted/50 min-h-[60vh] flex-1 rounded-xl md:min-h-min"/>
        </div>
    )
}
