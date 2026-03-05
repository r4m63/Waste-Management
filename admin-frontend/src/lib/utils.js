
import {clsx} from 'clsx'
import {twMerge} from 'tailwind-merge'

export function cn(...inputs) {
    return twMerge(clsx(inputs))
}


export function extractErrorMessage(errorData, fallback = "Произошла ошибка") {
    if (!errorData || typeof errorData !== 'object') {
        return fallback
    }

    if (errorData.errors && typeof errorData.errors === 'object') {
        const fieldErrors = Object.entries(errorData.errors)
            .map(([field, msg]) => `• ${field}: ${msg}`)
            .join('\n')
        
        if (fieldErrors) {
            return fieldErrors
        }
    }

    if (errorData.message && typeof errorData.message === 'string') {
        if (errorData.message !== "Request validation failed") {
            return errorData.message
        }
    }

    if (errorData.error && typeof errorData.error === 'string') {
        return errorData.error
    }

    return fallback
}


export async function parseApiError(response, fallback = "Произошла ошибка") {
    try {
        const data = await response.json()
        return extractErrorMessage(data, fallback)
    } catch {
        return `${fallback}: ${response.status} ${response.statusText}`
    }
}
