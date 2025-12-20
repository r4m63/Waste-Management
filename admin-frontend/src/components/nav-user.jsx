// components/nav-user.jsx

import {Bell, ChevronsUpDown, CircleUser, LogOut, Settings,} from "lucide-react"
import {useNavigate} from "react-router-dom"

import {Avatar, AvatarFallback, AvatarImage,} from "@/components/ui/avatar"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuGroup,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {SidebarMenu, SidebarMenuButton, SidebarMenuItem, useSidebar,} from "@/components/ui/sidebar"
import {useAuth} from "@/context/AuthContext.jsx"

export function NavUser({user}) {
    const {isMobile} = useSidebar()
    const {logout} = useAuth()
    const navigate = useNavigate()

    const handleLogout = async () => {
        await logout()
        navigate("/login", {replace: true})
    }

    return (
        <SidebarMenu>
            <SidebarMenuItem>
                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <SidebarMenuButton
                            size="lg"
                            className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                        >
                            <Avatar className="h-8 w-8 rounded-lg">
                                <AvatarImage src={user.avatar} alt={user.name}/>
                                <AvatarFallback className="rounded-lg">CN</AvatarFallback>
                            </Avatar>
                            <div className="grid flex-1 text-left text-sm leading-tight">
                                <span className="truncate font-medium">{user.name}</span>
                                <span className="truncate text-xs">{user.email}</span>
                            </div>
                            <ChevronsUpDown className="ml-auto size-4"/>
                        </SidebarMenuButton>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent
                        className="w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-lg"
                        side={isMobile ? "bottom" : "right"}
                        align="end"
                        sideOffset={4}
                    >
                        <DropdownMenuLabel className="p-0 font-normal">
                            <div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                                <Avatar className="h-8 w-8 rounded-lg">
                                    <AvatarImage src={user.avatar} alt={user.name}/>
                                    <AvatarFallback className="rounded-lg">CN</AvatarFallback>
                                </Avatar>
                                <div className="grid flex-1 text-left text-sm leading-tight">
                                    <span className="truncate font-medium">{user.name}</span>
                                    <span className="truncate text-xs">{user.email}</span>
                                </div>
                            </div>
                        </DropdownMenuLabel>
                        {/*<DropdownMenuSeparator/>*/}
                        {/*<DropdownMenuGroup>*/}
                        {/*    <DropdownMenuItem>*/}
                        {/*        <Sparkles/>*/}
                        {/*        Upgrade to Pro*/}
                        {/*    </DropdownMenuItem>*/}
                        {/*</DropdownMenuGroup>*/}
                        <DropdownMenuSeparator/>
                        <DropdownMenuGroup>
                            <DropdownMenuItem>
                                <CircleUser/>
                                Account
                            </DropdownMenuItem>
                            <DropdownMenuItem>
                                <Settings/>
                                Settings
                            </DropdownMenuItem>
                            <DropdownMenuItem>
                                <Bell/>
                                Notifications
                            </DropdownMenuItem>
                        </DropdownMenuGroup>
                        <DropdownMenuSeparator/>
                        <DropdownMenuItem
                            className="text-red-600 focus:bg-red-50 focus:text-red-700 dark:text-red-400 dark:focus:bg-red-950"
                            onClick={handleLogout}
                        >
                            <LogOut className="text-red-600 dark:text-red-400"/>
                            Log out
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            </SidebarMenuItem>
        </SidebarMenu>
    )
}
