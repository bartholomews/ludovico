# ludovico

Template from ["Fullstack Workflow with shadow-cljs"](
https://code.thheller.com/blog/shadow-cljs/2024/10/18/fullstack-cljs-workflow-with-shadow-cljs.html
)

## Quickstart

### Starting the REPL

```shell
npx shadow-cljs clj-repl
```

Go to http://localhost:9630

### Start the local server
`⌃ + ⌥ + ↩`

Go to http://localhost:18081

## Intellij/[Cursive](https://cursive-ide.com) dev setup

- "Import Project from Deps" via `deps.edn`

### CLJ REPL

Edit the "Clojure REPL / Remote" run config:
- `Use Port from file with localhost / Use standard port file`

### REPL Workflow

Create a new [REPL command](https://cursive-ide.com/userguide/repl.html#repl-commands),
- "Language and Frameworks" -> "Clojure" -> "REPL Commands"
- Give it a name, e.g. `repl/restart`
- "Before execution" -> "File Sync" -> `Sync all modified files`
- "Execution" -> """Execute command" -> `(require 'repl) (repl/restart)`

Then you can assign a "Keymap" to restart the server and reload changes with a single command
(e.g. `⌃ + ⌥ + ↩`)

If you want to reload files in the Cursive REPL and don't need to restart the dev server,
you can also use built-in Cursive commands - see [here](https://cursive-ide.com/userguide/repl.html#interaction-with-the-editor)

The dev server also starts `shadow-cljs` to watch the `public` directory.

- The shadow-cljs UI is available at http://localhost:9630  
- The [dev server + frontend](./src/dev/repl.clj) is available at http://localhost:18081

### CLJS REPL

The frontend is set up to hot reload automatically as part of `dev/repl.clj`.
You can switch to the CLJS REPL via `(shadow.cljs.devtools.api/repl :frontend)`

### Deployment

Build the production-optimized frontend assets

```shell
npx shadow-cljs release frontend
```

Start the [standalone server](./src/main/ludovico/server.clj) 
at http://localhost:3000

```shell
clj -M -m ludovico.server
```

## Resources

- https://shadow-cljs.github.io/docs/UsersGuide
- Example of CLJ + CLJS => https://github.com/thosmos/riverdb?tab=readme-ov-file

## Additional info

- Start cljs devtools via `clj` (as opposed to `npx`)
```shell
clj -M -m shadow.cljs.devtools.cli clj-repl
```

- If you need to run the frontend on a separate port (e.g. for dev env if it gets decoupled from the backend server),
  you can add `:dev-http {<PORT> "public"}` to `shadow-cljs.edn`.

- To spin a custom CLJS Repl in Intellij, edit the "Clojure REPL / Local" run config
    - Type of REPL: `clojure.main`
    - Parameters: `--main cljs.main`
  By default it starts at http://localhost:9000. 
  Validate the connection by typing `(js/alert "Hello CLJS!")` in the REPL.

## Known warnings

```
spec-tools.openapi.core/openapi-spec is deprecated
```
[]
This is an unused function in [spec-tool](https://github.com/metosin/spec-tools/blob/master/src/spec_tools/openapi/core.cljc#L174), 
the warning is due to a [known CLJS issue](https://clojure.atlassian.net/browse/CLJS-2000).
It will hopefully be removed in future versions of `coercion`.

- Interop with JS functions will cause warnings on `js/...` function calls,
it might be a good idea to disable `Editor -> Inspections -> Clojure -> Unresolved symbol` 
