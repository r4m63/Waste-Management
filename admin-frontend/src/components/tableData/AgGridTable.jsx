// components/data/AgGridTable.jsx

import React, {useEffect, useMemo, useState} from "react"
import {AgGridReact} from "ag-grid-react"

import {AllCommunityModule, colorSchemeDark, iconSetMaterial, ModuleRegistry, themeQuartz} from "ag-grid-community"
import {cn} from "@/lib/utils"

ModuleRegistry.registerModules([AllCommunityModule])

const agThemeLight = themeQuartz
    .withPart(iconSetMaterial)
    .withParams({
        backgroundColor: "hsl(var(--card))",
        foregroundColor: "hsl(var(--foreground))",

        headerBackgroundColor: "hsl(var(--muted))",
        headerTextColor: "hsl(var(--muted-foreground))",

        oddRowBackgroundColor: "hsl(var(--background))",
        evenRowBackgroundColor: "hsl(var(--card))",

        rowHoverColor: "hsl(var(--muted))",
        borderColor: "hsl(var(--border))",
        headerColumnResizeHandleColor: "hsl(var(--ring))",

        selectedRowBackgroundColor: "hsl(var(--accent))",
        selectedRowTextColor: "hsl(var(--accent-foreground))",
    })

const agThemeDark = themeQuartz
    .withPart(iconSetMaterial)
    .withPart(colorSchemeDark)
    .withParams({
        backgroundColor: "hsl(var(--card))",
        foregroundColor: "hsl(var(--foreground))",

        headerBackgroundColor: "hsl(var(--muted))",
        headerTextColor: "hsl(var(--muted-foreground))",

        oddRowBackgroundColor: "hsl(var(--background))",
        evenRowBackgroundColor: "hsl(var(--card))",

        rowHoverColor: "hsl(var(--muted))",
        borderColor: "hsl(var(--border))",
        headerColumnResizeHandleColor: "hsl(var(--ring))",

        selectedRowBackgroundColor: "hsl(var(--accent))",
        selectedRowTextColor: "hsl(var(--accent-foreground))",
    })


export function AgGridTable({
                                columnDefs,
                                rowData,
                                defaultColDef,
                                gridOptions,
                                className,
                                style,
                                height = "70vh",
                                ...rest
                            }) {

    const [isDark, setIsDark] = useState(false)

    useEffect(() => {
        if (typeof document === "undefined") return

        const root = document.documentElement

        const update = () => {
            setIsDark(root.classList.contains("dark"))
        }

        update()

        const observer = new MutationObserver(update)
        observer.observe(root, {attributes: true, attributeFilter: ["class"]})

        return () => observer.disconnect()
    }, [])

    const mergedDefaultColDef = useMemo(
        () => ({
            resizable: true,
            sortable: true,
            filter: true,
            flex: 1,
            minWidth: 140,
            ...defaultColDef,
        }),
        [defaultColDef],
    )

    const mergedGridOptions = useMemo(
        () => ({
            animateRows: true,
            suppressDragLeaveHidesColumns: true,
            rowData,
            columnDefs,
            defaultColDef: mergedDefaultColDef,
            ...gridOptions,
        }),
        [rowData, columnDefs, mergedDefaultColDef, gridOptions],
    )

    const theme = isDark ? agThemeDark : agThemeLight

    return (
        <div
            className={cn(
                "w-full rounded-lg border bg-card p-2 shadow-sm",
                className
            )}
            style={{
                height,
                minHeight: 360,
                ...style,
            }}
        >
            <AgGridReact
                theme={theme}
                {...mergedGridOptions}
                {...rest}
            />
        </div>
    )
}
