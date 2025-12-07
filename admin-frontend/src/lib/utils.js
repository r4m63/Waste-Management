// lib/utils.js

import {clsx} from 'clsx'
import {twMerge} from 'tailwind-merge'

export function cn(...inputs) {
    return twMerge(clsx(inputs))
}

/**
 * Извлекает читаемое сообщение об ошибке из ответа API.
 * Поддерживает форматы:
 * - { message: "..." }
 * - { error: "..." }
 * - { errors: { field: "message", ... } } (validation)
 * - { message: "...", errors: {...} }
 */
export function extractErrorMessage(errorData, fallback = "Произошла ошибка") {
    if (!errorData || typeof errorData !== 'object') {
        return fallback
    }

    // Если есть errors (объект с полями валидации), форматируем их
    if (errorData.errors && typeof errorData.errors === 'object') {
        const fieldErrors = Object.entries(errorData.errors)
            .map(([field, msg]) => `${field}: ${msg}`)
            .join('; ')
        
        if (fieldErrors) {
            return fieldErrors
        }
    }

    // Пробуем message
    if (errorData.message && typeof errorData.message === 'string') {
        // Пропускаем generic сообщения валидации
        if (errorData.message !== "Request validation failed") {
            return errorData.message
        }
    }

    // Пробуем error
    if (errorData.error && typeof errorData.error === 'string') {
        return errorData.error
    }

    return fallback
}

/**
 * Парсит ответ fetch и возвращает сообщение об ошибке
 */
export async function parseApiError(response, fallback = "Произошла ошибка") {
    try {
        const data = await response.json()
        return extractErrorMessage(data, fallback)
    } catch {
        return `${fallback}: ${response.status} ${response.statusText}`
    }
}
