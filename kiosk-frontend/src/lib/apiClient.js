export function apiFetch(url, options = {}) {
    const {headers, ...rest} = options;
    return fetch(url, {
        credentials: "include",
        headers: {
            ...headers,
        },
        ...rest,
    });
}
