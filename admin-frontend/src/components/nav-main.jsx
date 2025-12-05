// components/nav-main.jsx

import React from "react"
import {ChevronRight} from "lucide-react"
import {NavLink, useLocation} from "react-router-dom"

import {Collapsible, CollapsibleContent, CollapsibleTrigger,} from "@/components/ui/collapsible"
import {
    SidebarGroup,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarMenuSub,
    SidebarMenuSubButton,
    SidebarMenuSubItem,
} from "@/components/ui/sidebar"

const groupHasActiveItem = (groupItems = [], pathname) =>
    groupItems.some((subItem) => pathname.startsWith(subItem.url))

export function NavMain({items}) {
    const location = useLocation()
    const [openMap, setOpenMap] = React.useState(() =>
        Object.fromEntries(items.map((item) => [item.title, item.isActive || false])),
    )

    React.useEffect(() => {
        setOpenMap((prev) => {
            const next = {...prev}

            items.forEach((item) => {
                if (groupHasActiveItem(item.items, location.pathname)) {
                    next[item.title] = true
                }
            })

            return next
        })
    }, [items, location.pathname])

    return (
        <SidebarGroup>
            <SidebarGroupLabel>Навигация</SidebarGroupLabel>
            <SidebarMenu>
                {items.map((item) => (
                    <Collapsible
                        key={item.title}
                        asChild
                        open={openMap[item.title]}
                        onOpenChange={(isOpen) =>
                            setOpenMap((prev) => ({...prev, [item.title]: isOpen}))
                        }
                    >
                        <SidebarMenuItem>
                            <CollapsibleTrigger asChild>
                                <SidebarMenuButton className="group justify-between">
                                    <span className="flex items-center gap-2">
                                      <item.icon className="h-4 w-4"/>
                                      <span>{item.title}</span>
                                    </span>
                                    {item.items?.length ? (
                                        <ChevronRight
                                            className="
                                                  h-4 w-4
                                                  transition-transform duration-200
                                                  group-data-[state=open]:rotate-90
                                                "
                                        />
                                    ) : null}
                                </SidebarMenuButton>
                            </CollapsibleTrigger>

                            {item.items?.length ? (
                                <CollapsibleContent
                                    className="
                                          overflow-hidden
                                          data-[state=open]:animate-accordion-down
                                          data-[state=closed]:animate-accordion-up
                                        "
                                >
                                    <SidebarMenuSub>
                                        {item.items.map((subItem) => {
                                            const isActive = location.pathname.startsWith(subItem.url)

                                            return (
                                                <SidebarMenuSubItem key={subItem.title}>
                                                    <SidebarMenuSubButton asChild isActive={isActive}>
                                                        <NavLink to={subItem.url}>
                                                            <span>{subItem.title}</span>
                                                        </NavLink>
                                                    </SidebarMenuSubButton>
                                                </SidebarMenuSubItem>
                                            )
                                        })}
                                    </SidebarMenuSub>
                                </CollapsibleContent>
                            ) : null}
                        </SidebarMenuItem>
                    </Collapsible>
                ))}
            </SidebarMenu>
        </SidebarGroup>
    )
}
