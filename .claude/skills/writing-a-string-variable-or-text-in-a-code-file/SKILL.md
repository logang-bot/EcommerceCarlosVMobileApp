---
name: writing-a-string-variable-or-text-in-a-code-file
description: When writing strings or text inside any code file
---

1. **Move UI strings to `strings.xml`**: Any text that will be displayed to the user must be defined as a resource in `strings.xml`, never hardcoded in the source file.
2. **Reference UI strings with `stringResource`**: Inside a composable, always retrieve the string via `stringResource(R.string.your_key)`. In a ViewModel or non-composable context, use `context.getString(R.string.your_key)`.

The above rules do **not** apply to:
- Exception messages and error identifiers (internal, not user-facing)
- Log or debug strings
- Technical identifiers such as keys, tags, route names, or API field names