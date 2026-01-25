# TODO

## Current Sprint: Reagent 1.0 Parity

### Blockers
- [ ] Port remaining Preact tests from `todo-preact-tests-port.md`
- [ ] Verify all ported tests match Reagent behavior (not Preact/React)
- [ ] Test r/cursor and r/reaction thoroughly
- [ ] Support top level component that isn't a function

### High Priority
- [x] Fix tests broken by Squint v0.9.180 `pr-str` change (EDN output)
- [x] Fix tests using `^{:key ...}` syntax to use `(with-meta ...)` (reader meta is broken in Squint)
- [ ] Verify if Reagent supports :onClick (camelCase events) - if yes, implement
- [ ] r/with-let implementation
- [ ] Remove circularity and `declare`'s (code cleanup)

### Medium Priority
- [ ] Implement remaining lifecycle hooks from Reagent 1.0
  - Currently only :component-will-unmount (via :ref) is supported
  - Check which others Reagent 1.0 actually uses
- [ ] Re-export stuff to make interop with JS better?

### Testing Infrastructure
- [ ] Document how to create Scittle test apps for browser verification
- [ ] Automate reagent-runner comparisons in CI?

## Future Work: ClojureScript Compatibility

- [ ] Test `name` approach for keyword->string conversion
- [ ] Convert eucalypt.cljs to eucalypt.cljc if needed for macros
- [ ] Test in real ClojureScript project
- [ ] Document ClojureScript usage in README

## Optimization

**Current compiled size:** ~10k gzipped. Size is logged during build:
`pnpm build | grep -v squint | grep gzip:`

- [ ] Profile compiled size after each major change
- [ ] Look for opportunities to reduce duplication
- [ ] Consider inlining single-use helper functions

## Done

- [x] Make it reentrant
  - [x] Add multiple component render test & get it passing
  - [x] Remove ^:dynamic defs
  - [x] Remove clear-component-instances! from tests
- [x] Remove the :^private and defn- nonsense
- [x] Create a create script for npm init
- [x] Test & Demo JavaScript use of the library (thx borkdude)
- [x] :div#hi.hello style shorthand
- [x] Classes from vec
- [x] Kuro neko bug still there
- [x] Change the name of ratom to atom
- [x] Tests
- [x] Timer test is not updating when the page switches
- [x] List test is failing deletes
- [x] Ref test sticky-class bug
- [x] Demo Clock fn is firing every second in console event when it's not on (this is because timer is global)
- [x] Switch to clock then back to fragments test
- [x] Can we replace with-meta* and meta* with the real ones? [answer: not easily]
