// components/app-sidebar.jsx

import {
    Activity,
    AlertTriangle,
    CalendarClock,
    Command,
    LifeBuoy,
    MapPin,
    PieChart,
    Recycle,
    Route,
    Send,
    Settings2,
    Truck,
} from "lucide-react"
import {NavMain} from "@/components/nav-main"
import {NavProjects} from "@/components/nav-projects"
import {NavSecondary} from "@/components/nav-secondary"
import {NavUser} from "@/components/nav-user"
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar"


const data = {
    user: {
        name: "Dispatcher",
        email: "dispatcher@example.com",
        avatar: "/avatars/dispatcher.jpg",
    },
    navMain: [
        {
            title: "Точки и карта",
            icon: MapPin,
            isActive: true,
            items: [
                {
                    title: "Точки",
                    url: "/points",
                },
                // {
                //     title: "Карта точек",
                //     url: "/admin/points/map",
                // },
                // {
                //     title: "Статусы и заполненность",
                //     url: "/admin/points/status",
                // },
            ],
        },
        {
            title: "POS и жители",
            url: "/admin/pos",
            icon: Recycle,
            items: [
                {
                    title: "POS-терминалы",
                    url: "/admin/pos/terminals",
                },
                {
                    title: "Тарифы и фракции",
                    url: "/admin/pos/tariffs",
                },
                {
                    title: "Жители и баллы",
                    url: "/admin/pos/citizens",
                },
                {
                    title: "Операции / чеки",
                    url: "/admin/pos/operations",
                },
            ],
        },
        {
            title: "Транспорт и персонал",
            url: "/admin/resources",
            icon: Truck,
            items: [
                {
                    title: "ТС (автопарк)",
                    url: "/admin/resources/vehicles",
                },
                {
                    title: "Водители",
                    url: "/admin/resources/drivers",
                },
                {
                    title: "Смены водителей",
                    url: "/admin/resources/shifts",
                },
                {
                    title: "Депо и объекты сдачи",
                    url: "/admin/resources/depots",
                },
            ],
        },
        {
            title: "Планирование маршрутов",
            url: "/admin/planning",
            icon: Route,
            items: [
                {
                    title: "Потребности по точкам",
                    url: "/admin/planning/demand",
                },
                {
                    title: "Автопланирование",
                    url: "/admin/planning/auto",
                },
                {
                    title: "Маршруты (draft/planned)",
                    url: "/admin/planning/routes",
                },
                {
                    title: "Непокрытые точки",
                    url: "/admin/planning/uncovered",
                },
            ],
        },
        {
            title: "Выполнение рейсов",
            url: "/admin/execution",
            icon: Activity,
            items: [
                {
                    title: "Онлайн-мониторинг",
                    url: "/admin/execution/monitoring",
                },
                {
                    title: "История маршрутов",
                    url: "/admin/execution/history",
                },
                {
                    title: "События остановок",
                    url: "/admin/execution/stop-events",
                },
            ],
        },
        {
            title: "Инциденты",
            url: "/admin/incidents",
            icon: AlertTriangle,
            items: [
                {
                    title: "Журнал инцидентов",
                    url: "/admin/incidents/list",
                },
                {
                    title: "Типы инцидентов",
                    url: "/admin/incidents/types",
                },
            ],
        },
        {
            title: "Справочники и настройки",
            url: "/admin/settings",
            icon: Settings2,
            items: [
                {
                    title: "Справочники",
                    url: "/admin/settings/dictionaries",
                },
                {
                    title: "Настройки планировщика",
                    url: "/admin/settings/planner",
                },
                {
                    title: "Интеграции (карта, SMS)",
                    url: "/admin/settings/integrations",
                },
            ],
        },
    ],
    navSecondary: [
        {
            title: "Отчёты и аналитика",
            url: "/admin/reports",
            icon: PieChart,
        },
        {
            title: "Календарь смен и рейсов",
            url: "/admin/calendar",
            icon: CalendarClock,
        },
        {
            title: "Поддержка",
            url: "/support",
            icon: LifeBuoy,
        },
        {
            title: "Обратная связь",
            url: "/feedback",
            icon: Send,
        },
    ],
    projects: [
        {
            name: "Сегодняшний план рейсов",
            url: "/admin/planning/routes?date=today",
            icon: Route,
        },
        {
            name: "Активные маршруты",
            url: "/admin/execution/monitoring",
            icon: Activity,
        },
        {
            name: "Проблемные точки (инциденты)",
            url: "/admin/incidents/list?filter=open",
            icon: AlertTriangle,
        },
    ],
}


export function AppSidebar(props) {
    return (
        <Sidebar variant="inset" {...props}>
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton size="lg" asChild>
                            <a href="#">
                                <div
                                    className="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
                                    <Command className="size-4"/>
                                </div>
                                <div className="grid flex-1 text-left text-sm leading-tight">
                                    <span className="truncate font-medium">Acme Inc</span>
                                    <span className="truncate text-xs">Enterprise</span>
                                </div>
                            </a>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent className="wm-sidebar-scroll">
                <NavMain items={data.navMain}/>
                <NavProjects projects={data.projects}/>
                <NavSecondary items={data.navSecondary} className="mt-auto"/>
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={data.user}/>
            </SidebarFooter>
        </Sidebar>
    )
}
