// components/app-sidebar.jsx

import {
    Activity,
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
        name: "User",
        email: "user@example.com",
        avatar: "/avatars/dispatcher.jpg",
    },
    navMain: [
        {
            title: "Точки сбора и контейнеры",
            icon: MapPin,
            isActive: true,
            items: [
                {
                    title: "Точки",
                    url: "/points",
                },
                {
                    title: "Карта точек",
                    url: "/points/map",
                },
            ],
        },
        {
            title: "POS",
            icon: Recycle,
            items: [
                {
                    title: "POS-терминалы",
                    url: "/pos/terminals",
                },
                {
                    title: "Фракции и контейнеры",
                    url: "/pos/fractions",
                },
                {
                    title: "Заказы",
                    url: "/pos/operations",
                },
            ],
        },
        {
            title: "Транспорт и персонал",
            icon: Truck,
            items: [
                {
                    title: "ТС (автопарк)",
                    url: "/vehicles",
                },
                {
                    title: "Водители",
                    url: "/vehicles/drivers",
                },
                {
                    title: "Смены водителей",
                    url: "/resources/shifts",
                },
            ],
        },
        {
            title: "Маршруты",
            icon: Route,
            items: [
                {
                    title: "Планировние и маршруты",
                    url: "/planning/routes",
                },
                {
                    title: "События остановок",
                    url: "/execution/stop-events",
                },
                {
                    title: "Инциденты",
                    url: "/execution/stop-events",
                },

            ],
        },

    ],
    navSecondary: [
        {
            title: "Отчёты и аналитика",
            url: "/reports",
            icon: PieChart,
        },
        {
            title: "Календарь смен и рейсов",
            url: "/calendar",
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
}


export function AppSidebar(props) {
    return (
        <Sidebar variant="inset" {...props}>
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton size="lg" asChild>
                            <div>
                                <div
                                    className="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
                                    <Command className="size-4"/>
                                </div>
                                <div className="grid flex-1 text-left text-sm leading-tight">
                                    <span className="truncate font-medium">Waste Management</span>
                                    <span className="truncate text-xs">Admin Panel</span>
                                </div>
                            </div>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent className="wm-sidebar-scroll">
                <NavMain items={data.navMain}/>
                <NavSecondary items={data.navSecondary} className="mt-auto"/>
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={data.user}/>
            </SidebarFooter>
        </Sidebar>
    )
}
