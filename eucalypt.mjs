import * as squint_core from 'squint-cljs/core.js';
import * as squint from 'squint-cljs/core.js';
var core_atom = squint.atom;
var empty_js_map = function () {
return (new Map());

};
var default_namespace = "html";
var namespaces = ({"html": ({"uri": "http://www.w3.org/1999/xhtml"}), "svg": ({"uri": "http://www.w3.org/2000/svg", "entry-tags": (new Set (["svg"])), "boundary-tags": (new Set (["foreignObject"]))}), "math": ({"uri": "http://www.w3.org/1998/Math/MathML", "entry-tags": (new Set (["math"])), "boundary-tags": (new Set (["annotation-xml"]))})});
var entry_tag__GT_namespace = squint_core.into(({}), squint_core.mapcat((function (p__23) {
const vec__16 = p__23;
const ns7 = squint_core.nth(vec__16, 0, null);
const map__48 = squint_core.nth(vec__16, 1, null);
const map__49 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__48))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__48))) ? (map__48) : (squint_core.seq_to_map_for_destructuring(map__48)))) : (map__48));
const entry_tags10 = squint_core.get(map__49, "entry-tags");
return squint_core.map((function (tag) {
return [tag, ns7];

}), entry_tags10);

}), namespaces));
var uri__GT_namespace = squint_core.into(({}), squint_core.keep((function (p__24) {
const vec__16 = p__24;
const ns7 = squint_core.nth(vec__16, 0, null);
const map__48 = squint_core.nth(vec__16, 1, null);
const map__49 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__48))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__48))) ? (map__48) : (squint_core.seq_to_map_for_destructuring(map__48)))) : (map__48));
const uri10 = squint_core.get(map__49, "uri");
if (squint_core.truth_(uri10)) {
return [uri10, ns7];
};

}), namespaces));
var _STAR_watcher_STAR_ = ({val: null});
var roots = core_atom(empty_js_map());
var namespace_uri = function (ns_key) {
const or__23646__auto__1 = squint_core.get_in(namespaces, [ns_key, "uri"]);
if (squint_core.truth_(or__23646__auto__1)) {
return or__23646__auto__1} else {
return squint_core.get_in(namespaces, [default_namespace, "uri"])};

};
var normalize_namespace = function (uri) {
const candidate1 = (() => {
const or__23646__auto__2 = uri;
if (squint_core.truth_(or__23646__auto__2)) {
return or__23646__auto__2} else {
return namespace_uri(default_namespace)};

})();
if (squint_core.truth_(squint_core.contains_QMARK_(uri__GT_namespace, candidate1))) {
return candidate1} else {
return namespace_uri(default_namespace)};

};
var namespace_key = function (uri) {
return squint_core.get(uri__GT_namespace, uri, default_namespace);

};
var next_namespace = function (current, tag_name) {
const current_uri1 = normalize_namespace(current);
const current_key2 = namespace_key(current_uri1);
const boundary_tags3 = squint_core.get_in(namespaces, [current_key2, "boundary-tags"], (new Set ([])));
const enter_target4 = squint_core.get(entry_tag__GT_namespace, tag_name);
if (squint_core.truth_(enter_target4)) {
return namespace_uri(enter_target4)} else {
if (squint_core.truth_(squint_core.contains_QMARK_(boundary_tags3, tag_name))) {
return namespace_uri(default_namespace)} else {
if ("else") {
return current_uri1} else {
return null}}};

};
var dom__GT_namespace = function (dom) {
if (!(dom == null)) {
return normalize_namespace(dom.namespaceURI)} else {
return namespace_uri(default_namespace)};

};
var with_meta_STAR_ = function (obj, m) {
(obj["---meta"] = m);
return obj;

};
var meta_STAR_ = function (obj) {
return obj["---meta"];

};
var remove_watcher_from_runtime_queue_BANG_ = function (watcher) {
const temp__23239__auto__1 = squint_core.get(meta_STAR_(watcher), "runtime");
if (squint_core.truth_(temp__23239__auto__1)) {
const runtime2 = temp__23239__auto__1;
return squint_core.swap_BANG_(runtime2, squint_core.update, "pending-watchers", (function (queue) {
const existing3 = (() => {
const or__23646__auto__4 = queue;
if (squint_core.truth_(or__23646__auto__4)) {
return or__23646__auto__4} else {
return []};

})();
return squint_core.into([], squint_core.remove((function (_PERCENT_1) {
return squint_core._EQ_(watcher, _PERCENT_1);

}), existing3));

}));
};

};
var watcher_entry_key = function (watcher) {
const or__23646__auto__1 = squint_core.get(meta_STAR_(watcher), "normalized-component");
if (squint_core.truth_(or__23646__auto__1)) {
return or__23646__auto__1} else {
return watcher};

};
var register_watcher_with_host_BANG_ = function (host, watchers_atom, watcher) {
const meta_info1 = meta_STAR_(watcher);
const runtime2 = squint_core.get(meta_info1, "runtime");
const component_key3 = squint_core.get(meta_info1, "normalized-component");
if (squint_core.truth_((() => {
const and__23675__auto__4 = runtime2;
if (squint_core.truth_(and__23675__auto__4)) {
const and__23675__auto__5 = component_key3;
if (squint_core.truth_(and__23675__auto__5)) {
return host} else {
return and__23675__auto__5};
} else {
return and__23675__auto__4};

})())) {
return squint_core.swap_BANG_(runtime2, (function (state) {
const existing6 = squint_core.get_in(state, ["subscriptions", component_key3, host]);
if (squint_core._EQ_(watcher, squint_core.get(existing6, "watcher"))) {
return state} else {
const subs7 = (() => {
const or__23646__auto__8 = squint_core.get(state, "subscriptions");
if (squint_core.truth_(or__23646__auto__8)) {
return or__23646__auto__8} else {
return empty_js_map()};

})();
const component_map9 = (() => {
const or__23646__auto__10 = squint_core.get(subs7, component_key3);
if (squint_core.truth_(or__23646__auto__10)) {
return or__23646__auto__10} else {
return empty_js_map()};

})();
const entry11 = ({"host": host, "watchers-atom": watchers_atom, "watcher": watcher});
const new_component_map12 = squint_core.assoc(component_map9, host, entry11);
const new_subs13 = squint_core.assoc(subs7, component_key3, new_component_map12);
return squint_core.assoc(state, "subscriptions", new_subs13);
};

}));
};

};
var ensure_watcher_registered_BANG_ = function (host, watchers_atom) {
if (squint_core.truth_(_STAR_watcher_STAR_.val)) {
const watcher_key1 = watcher_entry_key(_STAR_watcher_STAR_.val);
if (squint_core.truth_(squint_core.contains_QMARK_(squint_core.deref(watchers_atom), watcher_key1))) {
} else {
squint_core.swap_BANG_(watchers_atom, squint_core.assoc, watcher_key1, _STAR_watcher_STAR_.val)};
return register_watcher_with_host_BANG_(host, watchers_atom, _STAR_watcher_STAR_.val);
};

};
var render_state_runtime = function (render_state) {
if (squint_core.truth_(render_state)) {
return squint_core.get(squint_core.deref(render_state), "runtime");
};

};
var runtime_component_cache = function (runtime) {
if (squint_core.truth_(runtime)) {
return squint_core.get(squint_core.deref(runtime), "component-instances");
};

};
var update_component_cache_BANG_ = function (runtime, update_fn) {
if (squint_core.truth_(runtime)) {
return squint_core.swap_BANG_(runtime, squint_core.update, "component-instances", (function (instances) {
return update_fn((() => {
const or__23646__auto__1 = instances;
if (squint_core.truth_(or__23646__auto__1)) {
return or__23646__auto__1} else {
return empty_js_map()};

})());

}));
};

};
var runtime_mounted_info = function (runtime, normalized_component) {
if (squint_core.truth_(runtime)) {
return squint_core.get(squint_core.get(squint_core.deref(runtime), "mounted-components"), normalized_component);
};

};
var assoc_runtime_mounted_info_BANG_ = function (runtime, normalized_component, info) {
if (squint_core.truth_(runtime)) {
return squint_core.swap_BANG_(runtime, squint_core.update, "mounted-components", (function (components) {
return squint_core.assoc((() => {
const or__23646__auto__1 = components;
if (squint_core.truth_(or__23646__auto__1)) {
return or__23646__auto__1} else {
return empty_js_map()};

})(), normalized_component, info);

}));
};

};
var create_render_state = function (p__25) {
const map__13 = p__25;
const map__14 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__13))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__13))) ? (map__13) : (squint_core.seq_to_map_for_destructuring(map__13)))) : (map__13));
const normalized_component5 = squint_core.get(map__14, "normalized-component");
const container6 = squint_core.get(map__14, "container");
const base_namespace7 = squint_core.get(map__14, "base-namespace");
const runtime8 = squint_core.get(map__14, "runtime");
const state9 = ({"active": true, "positional-key-counter": 0, "base-namespace": normalize_namespace(base_namespace7)});
const state10 = (() => {
const G__2611 = state9;
const G__2612 = ((squint_core.truth_(normalized_component5)) ? (({...G__2611,["normalized-component"]:normalized_component5})) : (G__2611));
const G__2613 = ((squint_core.truth_(container6)) ? (squint_core.assoc(G__2612, "container", container6)) : (G__2612));
if (squint_core.truth_(runtime8)) {
return squint_core.assoc(G__2613, "runtime", runtime8)} else {
return G__2613};

})();
return core_atom(state10);

};
var next_positional_key_BANG_ = function (render_state) {
const next_val1 = (squint_core.get(squint_core.deref(render_state), "positional-key-counter") + 1);
squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", next_val1);
return next_val1;

};
var run_watcher_now = function (watcher) {
const old_watcher1 = _STAR_watcher_STAR_.val;
try{
_STAR_watcher_STAR_.val = watcher;
return watcher();
}
finally{
_STAR_watcher_STAR_.val = old_watcher1}
;

};
var flush_queued_watchers = function (runtime) {
const queued1 = squint_core.get(squint_core.deref(runtime), "pending-watchers");
squint_core.swap_BANG_(runtime, (function (state) {
return squint_core.assoc(state, "pending-watchers", [], "watcher-flush-scheduled?", false);

}));
for (let G__2 of squint_core.iterable(queued1)) {
const watcher3 = G__2;
run_watcher_now(watcher3)
}
return null;

};
var schedule_watcher_flush_BANG_ = function (runtime) {
if (squint_core.truth_((() => {
const and__23675__auto__1 = runtime;
if (squint_core.truth_(and__23675__auto__1)) {
return squint_core.not(squint_core.get(squint_core.deref(runtime), "watcher-flush-scheduled?"))} else {
return and__23675__auto__1};

})())) {
squint_core.swap_BANG_(runtime, squint_core.assoc, "watcher-flush-scheduled?", true);
const flush_fn2 = (function () {
return flush_queued_watchers(runtime);

});
if (!(globalThis.queueMicrotask == null)) {
return globalThis.queueMicrotask(flush_fn2)} else {
return setTimeout(flush_fn2, 0)};
};

};
var queue_watcher_BANG_ = function (watcher) {
const temp__23159__auto__1 = squint_core.get(meta_STAR_(watcher), "runtime");
if (squint_core.truth_(temp__23159__auto__1)) {
const runtime2 = temp__23159__auto__1;
squint_core.swap_BANG_(runtime2, squint_core.update, "pending-watchers", squint_core.fnil(squint_core.conj, []), watcher);
return schedule_watcher_flush_BANG_(runtime2);
} else {
return run_watcher_now(watcher)};

};
var should_defer_watcher_QMARK_ = function (watcher) {
const meta_info1 = meta_STAR_(watcher);
const defer_fn2 = (() => {
const and__23675__auto__3 = meta_info1;
if (squint_core.truth_(and__23675__auto__3)) {
return squint_core.get(meta_info1, "should-defer?")} else {
return and__23675__auto__3};

})();
return squint_core.boolean$((() => {
const and__23675__auto__4 = squint_core.fn_QMARK_(defer_fn2);
if (squint_core.truth_(and__23675__auto__4)) {
return defer_fn2()} else {
return and__23675__auto__4};

})());

};
var with_watcher_bound = function (normalized_component, render_state, f) {
const old_watcher1 = _STAR_watcher_STAR_.val;
const runtime2 = render_state_runtime(render_state);
const watcher_fn3 = with_meta_STAR_((function () {
return modify_dom(runtime2, normalized_component);

}), ({"normalized-component": normalized_component, "should-defer?": (function () {
return squint_core.boolean$(squint_core.get(squint_core.deref(render_state), "active"));

}), "runtime": runtime2}));
try{
_STAR_watcher_STAR_.val = watcher_fn3;
return f();
}
finally{
_STAR_watcher_STAR_.val = old_watcher1}
;

};
var remove_watchers_for_component = function (runtime, normalized_component) {
const runtime_state1 = ((squint_core.truth_(runtime)) ? (squint_core.deref(runtime)) : (null));
const subscriptions2 = squint_core.get_in(runtime_state1, ["subscriptions", normalized_component]);
if (squint_core.truth_((() => {
const and__23675__auto__3 = runtime;
if (squint_core.truth_(and__23675__auto__3)) {
return normalized_component} else {
return and__23675__auto__3};

})())) {
if (squint_core.truth_(squint_core.seq(subscriptions2))) {
for (let G__4 of squint_core.iterable(squint_core.vals(subscriptions2))) {
const map__57 = G__4;
const map__58 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__57))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__57))) ? (map__57) : (squint_core.seq_to_map_for_destructuring(map__57)))) : (map__57));
const watchers_atom9 = squint_core.get(map__58, "watchers-atom");
const watcher10 = squint_core.get(map__58, "watcher");
remove_watcher_from_runtime_queue_BANG_(watcher10);
if (squint_core.truth_((() => {
const and__23675__auto__11 = watchers_atom9;
if (squint_core.truth_(and__23675__auto__11)) {
return watcher10} else {
return and__23675__auto__11};

})())) {
const key12 = watcher_entry_key(watcher10);
squint_core.swap_BANG_(watchers_atom9, (function (state) {
return squint_core.dissoc((() => {
const or__23646__auto__13 = state;
if (squint_core.truth_(or__23646__auto__13)) {
return or__23646__auto__13} else {
return empty_js_map()};

})(), key12);

}))}
}}};
squint_core.swap_BANG_(runtime, (function (state) {
const subs14 = (() => {
const or__23646__auto__15 = squint_core.get(state, "subscriptions");
if (squint_core.truth_(or__23646__auto__15)) {
return or__23646__auto__15} else {
return empty_js_map()};

})();
const new_subs16 = squint_core.dissoc(subs14, normalized_component);
return squint_core.assoc(state, "subscriptions", new_subs16);

}));
return null;

};
var remove_all_runtime_watchers_BANG_ = function (runtime) {
if (squint_core.truth_(runtime)) {
const components1 = squint_core.keys((() => {
const or__23646__auto__2 = squint_core.get(squint_core.deref(runtime), "subscriptions");
if (squint_core.truth_(or__23646__auto__2)) {
return or__23646__auto__2} else {
return empty_js_map()};

})());
for (let G__3 of squint_core.iterable(components1)) {
const component4 = G__3;
remove_watchers_for_component(runtime, component4)
}
return null;
};

};
var style_map__GT_css_str = function (style_map) {
return squint_core.apply(squint_core.str, squint_core.map((function (p__27) {
const vec__14 = p__27;
const k5 = squint_core.nth(vec__14, 0, null);
const v6 = squint_core.nth(vec__14, 1, null);
return `${k5??''}${":"}${v6??''}${";"}`;

}), style_map));

};
var get_event_name = function (k, tag_name) {
if (squint_core.truth_((("on-change" === k) && squint_core.get((new Set (["INPUT", "TEXTAREA"])), tag_name)))) {
return "oninput"} else {
if (("on-double-click" === k)) {
return "ondblclick"} else {
if ("else") {
return k.replaceAll("-", "")} else {
return null}}};

};
var assign_event_BANG_ = function (element, event_key, handler) {
const event_name1 = get_event_name(event_key, element.tagName);
return (element[event_name1] = handler);

};
var apply_style_BANG_ = function (element, style_map) {
if (squint_core.truth_(squint_core.not_empty(style_map))) {
return element.setAttribute("style", style_map__GT_css_str(style_map))} else {
return element.removeAttribute("style")};

};
var apply_class_BANG_ = function (element, class_val) {
const normalized1 = ((squint_core.truth_((() => {
const and__23675__auto__2 = squint_core.sequential_QMARK_(class_val);
if (squint_core.truth_(and__23675__auto__2)) {
return squint_core.not(squint_core.string_QMARK_(class_val))} else {
return and__23675__auto__2};

})())) ? (squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, class_val)).join(" ")) : (class_val));
if (squint_core.truth_((() => {
const or__23646__auto__3 = (normalized1 == null);
if (or__23646__auto__3) {
return or__23646__auto__3} else {
return squint_core._EQ_("", normalized1)};

})())) {
return element.removeAttribute("class")} else {
return element.setAttribute("class", normalized1)};

};
var apply_ref_change = function (element, old_ref, new_ref) {
if (!squint_core._EQ_(old_ref, new_ref)) {
if (squint_core.truth_(old_ref)) {
old_ref(null)};
if (squint_core.truth_(new_ref)) {
new_ref(element)};
return (element["---ref-fn"] = new_ref);
};

};
var set_or_remove_attribute_BANG_ = function (element, k, v) {
if (squint_core.truth_(k.startsWith("on-"))) {
return assign_event_BANG_(element, k, v)} else {
if (("style" === k)) {
return apply_style_BANG_(element, v)} else {
if (("class" === k)) {
return apply_class_BANG_(element, v)} else {
if (squint_core.truth_((() => {
const or__23646__auto__1 = ("checked" === k);
if (or__23646__auto__1) {
return or__23646__auto__1} else {
return ("selected" === k)};

})())) {
return (element[k] = v)} else {
if ("else") {
if ((v == null)) {
return element.removeAttribute(k)} else {
return element.setAttributeNS(null, k, v)}} else {
return null}}}}};

};
var set_attributes_BANG_ = function (element, attrs) {
apply_ref_change(element, null, squint_core.get(attrs, "ref"));
for (let G__1 of squint_core.iterable(attrs)) {
const vec__25 = G__1;
const k6 = squint_core.nth(vec__25, 0, null);
const v7 = squint_core.nth(vec__25, 1, null);
if (!(k6 === "ref")) {
if (("xmlns" === k6)) {
} else {
if ("else") {
set_or_remove_attribute_BANG_(element, k6, v7)} else {
}}}
}
return null;

};
var parse_tag = function (tag) {
const tag_str10 = `${tag??''}`;
const vec__111 = tag_str10.split("#", 2);
const before_hash12 = squint_core.nth(vec__111, 0, null);
const after_hash13 = squint_core.nth(vec__111, 1, null);
const vec__414 = before_hash12.split(/\./);
const seq__515 = squint_core.seq(vec__414);
const first__616 = squint_core.first(seq__515);
const seq__517 = squint_core.next(seq__515);
const tag_name_str18 = first__616;
const classes_from_before_hash19 = seq__517;
const tag_name20 = ((squint_core.truth_(squint_core.empty_QMARK_(tag_name_str18))) ? ("div") : (tag_name_str18));
const vec__721 = ((squint_core.truth_(after_hash13)) ? (after_hash13.split(/\./)) : ([]));
const seq__822 = squint_core.seq(vec__721);
const first__923 = squint_core.first(seq__822);
const seq__824 = squint_core.next(seq__822);
const id25 = first__923;
const classes_from_after_hash26 = seq__824;
const all_classes27 = squint_core.vec(squint_core.remove(squint_core.empty_QMARK_, squint_core.concat(classes_from_before_hash19, classes_from_after_hash26)));
return ({"tag-name": tag_name20, "id": id25, "classes": ((squint_core.truth_(squint_core.seq(all_classes27))) ? (all_classes27) : (null))});

};
var parse_hiccup = function (hiccup) {
const vec__16 = hiccup;
const seq__27 = squint_core.seq(vec__16);
const first__38 = squint_core.first(seq__27);
const seq__29 = squint_core.next(seq__27);
const tag_keyword10 = first__38;
const content11 = seq__29;
const map__412 = parse_tag(tag_keyword10);
const map__413 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__412))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__412))) ? (map__412) : (squint_core.seq_to_map_for_destructuring(map__412)))) : (map__412));
const tag_name14 = squint_core.get(map__413, "tag-name");
const id15 = squint_core.get(map__413, "id");
const classes16 = squint_core.get(map__413, "classes");
const attrs_from_hiccup17 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content11)))) ? (squint_core.first(content11)) : (({})));
const final_id18 = (() => {
const or__23646__auto__19 = squint_core.get(attrs_from_hiccup17, "id");
if (squint_core.truth_(or__23646__auto__19)) {
return or__23646__auto__19} else {
return id15};

})();
const class_from_hiccup20 = squint_core.get(attrs_from_hiccup17, "class");
const all_classes21 = (() => {
const tag_classes22 = (() => {
const or__23646__auto__23 = classes16;
if (squint_core.truth_(or__23646__auto__23)) {
return or__23646__auto__23} else {
return []};

})();
const attr_classes24 = (((class_from_hiccup20 == null)) ? ([]) : (((squint_core.truth_(squint_core.string_QMARK_(class_from_hiccup20))) ? ([class_from_hiccup20]) : (((squint_core.truth_((() => {
const and__23675__auto__25 = squint_core.sequential_QMARK_(class_from_hiccup20);
if (squint_core.truth_(and__23675__auto__25)) {
return squint_core.not(squint_core.string_QMARK_(class_from_hiccup20))} else {
return and__23675__auto__25};

})())) ? (squint_core.vec(class_from_hiccup20)) : ((("else") ? ([class_from_hiccup20]) : (null))))))));
const combined26 = squint_core.vec(squint_core.concat(tag_classes22, attr_classes24));
if (squint_core.truth_(squint_core.seq(combined26))) {
return combined26;
};

})();
const attrs_with_id27 = ((squint_core.truth_(final_id18)) ? (squint_core.assoc(attrs_from_hiccup17, "id", final_id18)) : (attrs_from_hiccup17));
const final_attrs28 = ((!(all_classes21 == null)) ? (squint_core.assoc(attrs_with_id27, "class", all_classes21)) : (squint_core.dissoc(attrs_with_id27, "class")));
const final_content29 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content11)))) ? (squint_core.rest(content11)) : (content11));
return ({"tag-name": tag_name14, "attrs": final_attrs28, "content": final_content29});

};
var create_element = function (hiccup, current_ns, render_state) {
const map__13 = parse_hiccup(hiccup);
const map__14 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__13))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__13))) ? (map__13) : (squint_core.seq_to_map_for_destructuring(map__13)))) : (map__13));
const tag_name5 = squint_core.get(map__14, "tag-name");
const attrs6 = squint_core.get(map__14, "attrs");
const content7 = squint_core.get(map__14, "content");
const value8 = squint_core.get(attrs6, "value");
const danger_html9 = squint_core.get_in(attrs6, ["dangerouslySetInnerHTML", "__html"]);
const attrs_without_value10 = squint_core.dissoc(attrs6, "value", "dangerouslySetInnerHTML");
const current_ns_normalized11 = normalize_namespace(current_ns);
const element_ns12 = ((squint_core.truth_(squint_core.get(entry_tag__GT_namespace, tag_name5))) ? (next_namespace(current_ns_normalized11, tag_name5)) : (current_ns_normalized11));
const children_ns13 = next_namespace(element_ns12, tag_name5);
const element14 = document.createElementNS(element_ns12, tag_name5);
set_attributes_BANG_(element14, attrs_without_value10);
if (!(danger_html9 == null)) {
element14.innerHTML = danger_html9} else {
for (let G__15 of squint_core.iterable(content7)) {
const child16 = G__15;
const temp__23239__auto__17 = hiccup__GT_dom(child16, children_ns13, render_state);
if (squint_core.truth_(temp__23239__auto__17)) {
const child_node18 = temp__23239__auto__17;
element14.appendChild(child_node18)}
}};
if (!(value8 == null)) {
if (squint_core.truth_((("SELECT" === element14.tagName) && element14.multiple))) {
const value_set19 = squint_core.set(value8);
for (let G__20 of squint_core.iterable(element14.options)) {
const opt21 = G__20;
(opt21["selected"] = squint_core.contains_QMARK_(value_set19, opt21.value))
}} else {
(element14["value"] = value8)}};
return element14;

};
var component__GT_hiccup = function (normalized_component) {
const vec__14 = normalized_component;
const seq__25 = squint_core.seq(vec__14);
const first__36 = squint_core.first(seq__25);
const seq__27 = squint_core.next(seq__25);
const config8 = first__36;
const params9 = seq__27;
const reagent_render10 = squint_core.get(config8, "reagent-render");
return squint_core.apply(reagent_render10, params9);

};
var fetch_or_create_component_instance = function (a_fn, params_vec, component_meta, render_state) {
const runtime1 = render_state_runtime(render_state);
const component_cache2 = runtime_component_cache(runtime1);
const fn_cache3 = ((squint_core.truth_(component_cache2)) ? (squint_core.get(component_cache2, a_fn)) : (null));
const instance_key4 = ((squint_core.truth_(squint_core.contains_QMARK_(component_meta, "key"))) ? (squint_core.get(component_meta, "key")) : (((squint_core.truth_(render_state)) ? (next_positional_key_BANG_(render_state)) : (squint_core.random_uuid()))));
const cached_instance5 = (() => {
const or__23646__auto__6 = squint_core.get_in(fn_cache3, [instance_key4, "instance"]);
if (squint_core.truth_(or__23646__auto__6)) {
return or__23646__auto__6} else {
return squint_core.get_in(fn_cache3, ["form-1-instance", "instance"])};

})();
const or__23646__auto__7 = cached_instance5;
if (squint_core.truth_(or__23646__auto__7)) {
return or__23646__auto__7} else {
const func_or_hiccup11 = squint_core.apply(a_fn, params_vec);
const vec__812 = ((squint_core.truth_(squint_core.fn_QMARK_(func_or_hiccup11))) ? ([({"reagent-render": func_or_hiccup11}), instance_key4, "form-2"]) : ([({"reagent-render": a_fn}), "form-1-instance", "form-1"]));
const instance13 = squint_core.nth(vec__812, 0, null);
const cache_key14 = squint_core.nth(vec__812, 1, null);
const type15 = squint_core.nth(vec__812, 2, null);
update_component_cache_BANG_(runtime1, (function (cache) {
const fn_cache16 = (() => {
const or__23646__auto__17 = squint_core.get(cache, a_fn);
if (squint_core.truth_(or__23646__auto__17)) {
return or__23646__auto__17} else {
return empty_js_map()};

})();
const new_fn_cache18 = squint_core.assoc(fn_cache16, cache_key14, ({"type": type15, "instance": instance13}));
return squint_core.assoc(cache, a_fn, new_fn_cache18);

}));
return instance13;
};

};
var normalize_component = function (component, render_state) {
if (squint_core.truth_(squint_core.vector_QMARK_(component))) {
const first_element1 = component[0];
const params2 = squint_core.subvec(component, 1);
if (squint_core.truth_(squint_core.fn_QMARK_(first_element1))) {
const instance3 = fetch_or_create_component_instance(first_element1, squint_core.vec(params2), squint_core.meta(component), render_state);
return squint_core.into([instance3], params2);
} else {
if (squint_core.truth_(squint_core.string_QMARK_(first_element1))) {
return squint_core.into([({"reagent-render": (function () {
return component;

})})], params2)} else {
if (squint_core.truth_(squint_core.map_QMARK_(first_element1))) {
const component_as_map4 = first_element1;
const render_fn5 = squint_core.get(component_as_map4, "reagent-render");
const comp_with_lifecycle6 = ({"reagent-render": render_fn5});
return squint_core.into([comp_with_lifecycle6], params2);
} else {
return null}}};
};

};
var expand_hiccup = function (hiccup, render_state) {
let hiccup_SINGLEQUOTE_1 = hiccup;
while(true){
if (squint_core.truth_((() => {
const and__23675__auto__2 = squint_core.vector_QMARK_(hiccup_SINGLEQUOTE_1);
if (squint_core.truth_(and__23675__auto__2)) {
return squint_core.fn_QMARK_(squint_core.first(hiccup_SINGLEQUOTE_1))} else {
return and__23675__auto__2};

})())) {
let G__3 = component__GT_hiccup(normalize_component(hiccup_SINGLEQUOTE_1, render_state));
hiccup_SINGLEQUOTE_1 = G__3;
continue;
} else {
if (squint_core.truth_((() => {
const and__23675__auto__4 = squint_core.map_QMARK_(hiccup_SINGLEQUOTE_1);
if (squint_core.truth_(and__23675__auto__4)) {
return squint_core.get(hiccup_SINGLEQUOTE_1, "reagent-render")} else {
return and__23675__auto__4};

})())) {
let G__5 = squint_core.get(hiccup_SINGLEQUOTE_1, "reagent-render")();
hiccup_SINGLEQUOTE_1 = G__5;
continue;
} else {
if ("else") {
return hiccup_SINGLEQUOTE_1} else {
return null}}};
;break;
}
;

};
var hiccup__GT_dom = /* @__PURE__ */ (() => {
const impl311 = (function (hiccup, render_state) {
return hiccup__GT_dom(hiccup, namespace_uri(default_namespace), render_state);

});
const impl322 = (function (hiccup, current_ns, render_state) {
const hiccup3 = expand_hiccup(hiccup, render_state);
const result4 = ((squint_core.truth_((() => {
const or__23646__auto__5 = squint_core.string_QMARK_(hiccup3);
if (squint_core.truth_(or__23646__auto__5)) {
return or__23646__auto__5} else {
return squint_core.number_QMARK_(hiccup3)};

})())) ? (document.createTextNode(`${hiccup3??''}`)) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup3))) ? ((() => {
const tag6 = hiccup3[0];
if (("<>" === tag6)) {
const fragment7 = document.createDocumentFragment();
for (let G__8 of squint_core.iterable(squint_core.rest(hiccup3))) {
const child9 = G__8;
const temp__23239__auto__10 = hiccup__GT_dom(child9, current_ns, render_state);
if (squint_core.truth_(temp__23239__auto__10)) {
const child_node11 = temp__23239__auto__10;
fragment7.appendChild(child_node11)}
};
return fragment7;
} else {
return create_element(hiccup3, current_ns, render_state)};

})()) : (((squint_core.truth_(squint_core.seq_QMARK_(hiccup3))) ? ((() => {
const fragment12 = document.createDocumentFragment();
for (let G__13 of squint_core.iterable(hiccup3)) {
const item14 = G__13;
const item_with_meta15 = ((squint_core.truth_((() => {
const and__23675__auto__16 = squint_core.vector_QMARK_(item14);
if (squint_core.truth_(and__23675__auto__16)) {
return squint_core.meta(item14)} else {
return and__23675__auto__16};

})())) ? (squint_core.with_meta(item14, squint_core.meta(item14))) : (item14));
const temp__23239__auto__17 = hiccup__GT_dom(item_with_meta15, current_ns, render_state);
if (squint_core.truth_(temp__23239__auto__17)) {
const child_node18 = temp__23239__auto__17;
fragment12.appendChild(child_node18)}
};
return fragment12;

})()) : (((squint_core.truth_((() => {
const or__23646__auto__19 = (hiccup3 == null);
if (or__23646__auto__19) {
return or__23646__auto__19} else {
return squint_core.boolean_QMARK_(hiccup3)};

})())) ? (null) : ((("else") ? (document.createTextNode(`${hiccup3??''}`)) : (null))))))))));
return result4;

});
const f28 = (function (...args29) {
const self3320 = this;
const G__3421 = args29.length;
switch (G__3421) {case 2:
return impl311.call(self3320, args29[0], args29[1]);

break;
case 3:
return impl322.call(self3320, args29[0], args29[1], args29[2]);

break;
default:
throw (new Error(`${"Invalid arity: "}${args29.length??''}`))};

});
return f28;

})();
var get_hiccup_children = function (hiccup) {
const content1 = squint_core.rest(hiccup);
if (squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content1)))) {
return squint_core.rest(content1)} else {
return content1};

};
var hiccup_seq_QMARK_ = function (x) {
const and__23675__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23675__auto__1)) {
return (squint_core.not(squint_core.string_QMARK_(x)) && squint_core.not(squint_core.vector_QMARK_(x)))} else {
return and__23675__auto__1};

};
var fully_render_hiccup = function (hiccup, render_state) {
const hiccup1 = expand_hiccup(hiccup, render_state);
const result2 = (((hiccup1 == null)) ? (null) : (((squint_core.truth_(hiccup_seq_QMARK_(hiccup1))) ? (squint_core.mapv((function (_PERCENT_1) {
return fully_render_hiccup(_PERCENT_1, render_state);

}), hiccup1)) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup1))) ? ((() => {
const attrs3 = (() => {
const _QMARK_attrs4 = hiccup1[1];
if (squint_core.truth_(squint_core.map_QMARK_(_QMARK_attrs4))) {
return _QMARK_attrs4;
};

})();
const children5 = ((squint_core.truth_(attrs3)) ? (squint_core.subvec(hiccup1, 2)) : (squint_core.subvec(hiccup1, 1)));
const head6 = ((squint_core.truth_(attrs3)) ? ([hiccup1[0], attrs3]) : ([hiccup1[0]]));
return squint_core.into(head6, squint_core.reduce((function (acc, child) {
const processed7 = fully_render_hiccup(child, render_state);
if ((processed7 == null)) {
return acc} else {
if (squint_core.truth_((() => {
const and__23675__auto__8 = squint_core.vector_QMARK_(processed7);
if (squint_core.truth_(and__23675__auto__8)) {
return ("<>" === processed7[0])} else {
return and__23675__auto__8};

})())) {
return squint_core.into(acc, squint_core.subvec(processed7, 1))} else {
if (squint_core.truth_(hiccup_seq_QMARK_(child))) {
return squint_core.into(acc, processed7)} else {
if ("else") {
return squint_core.conj(acc, processed7)} else {
return null}}}};

}), [], children5));

})()) : ((("else") ? (hiccup1) : (null))))))));
return result2;

};
var unmount_node_and_children = function (node) {
if (squint_core.truth_(node)) {
const temp__23239__auto__1 = node["---ref-fn"];
if (squint_core.truth_(temp__23239__auto__1)) {
const ref_fn2 = temp__23239__auto__1;
ref_fn2(null);
(node["---ref-fn"] = null)};
for (let G__3 of squint_core.iterable(squint_core.vec(node["childNodes"]))) {
const child4 = G__3;
unmount_node_and_children(child4)
}
return null;
};

};
var remove_node_and_unmount_BANG_ = function (node) {
if (squint_core.truth_(node)) {
unmount_node_and_children(node);
return node.remove();
};

};
var patch_children = function (hiccup_a_rendered, hiccup_b_rendered, dom_a, render_state) {
const children_a1 = squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, get_hiccup_children(hiccup_a_rendered)));
const children_b2 = squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, get_hiccup_children(hiccup_b_rendered)));
const len_a3 = squint_core.count(children_a1);
const len_b4 = squint_core.count(children_b2);
const parent_ns5 = dom__GT_namespace(dom_a);
let i6 = 0;
while(true){
if ((i6 < squint_core.min(len_a3, len_b4))) {
const child_a7 = squint_core.nth(children_a1, i6);
const child_b8 = squint_core.nth(children_b2, i6);
const dom_node9 = dom_a.childNodes[i6];
patch(child_a7, child_b8, dom_node9, render_state);
let G__10 = (i6 + 1);
i6 = G__10;
continue;
};break;
}
;
if ((len_b4 > len_a3)) {
for (let G__11 of squint_core.iterable(squint_core.range(len_a3, len_b4))) {
const i12 = G__11;
const temp__23239__auto__13 = hiccup__GT_dom(squint_core.nth(children_b2, i12), parent_ns5, render_state);
if (squint_core.truth_(temp__23239__auto__13)) {
const new_child14 = temp__23239__auto__13;
dom_a.appendChild(new_child14)}
}};
if ((len_a3 > len_b4)) {
const n3515 = (len_a3 - len_b4);
let _16 = 0;
for (;_16<n3515;_16++) {
remove_node_and_unmount_BANG_(dom_a.lastChild)
};
return null;
};

};
var get_attrs = function (hiccup) {
const s1 = squint_core.second(hiccup);
if (squint_core.truth_(squint_core.map_QMARK_(s1))) {
return s1} else {
return ({})};

};
var patch_attributes = function (hiccup_a_rendered, hiccup_b_rendered, dom_a) {
const a_attrs1 = get_attrs(hiccup_a_rendered);
const b_attrs2 = get_attrs(hiccup_b_rendered);
apply_ref_change(dom_a, squint_core.get(a_attrs1, "ref"), squint_core.get(b_attrs2, "ref"));
const all_keys3 = squint_core.set(squint_core.concat(squint_core.keys(a_attrs1), squint_core.keys(b_attrs2)));
for (let G__4 of squint_core.iterable(all_keys3)) {
const k5 = G__4;
if (squint_core.truth_((!(k5 === "ref") && (!(k5 === "xmlns") && (!(k5 === "value") && !(k5 === "dangerouslySetInnerHTML")))))) {
const old_v6 = squint_core.get(a_attrs1, k5);
const new_v7 = squint_core.get(b_attrs2, k5);
if (!squint_core._EQ_(old_v6, new_v7)) {
set_or_remove_attribute_BANG_(dom_a, k5, new_v7)}}
}
return null;

};
var realize_deep = function (x) {
if (squint_core.truth_((() => {
const and__23675__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23675__auto__1)) {
return x["gen"]} else {
return and__23675__auto__1};

})())) {
return squint_core.mapv(realize_deep, x)} else {
if (squint_core.truth_((() => {
const and__23675__auto__2 = squint_core.sequential_QMARK_(x);
if (squint_core.truth_(and__23675__auto__2)) {
return squint_core.not(squint_core.string_QMARK_(x))} else {
return and__23675__auto__2};

})())) {
return squint_core.into(squint_core.empty(x), squint_core.map(realize_deep, x))} else {
if ("else") {
return x} else {
return null}}};

};
var patch = function (hiccup_a_rendered, hiccup_b_rendered, dom_a, render_state) {
if ((hiccup_a_rendered === hiccup_b_rendered)) {
return dom_a} else {
const hiccup_a_realized1 = realize_deep(hiccup_a_rendered);
const hiccup_b_realized2 = realize_deep(hiccup_b_rendered);
if (squint_core._EQ_(hiccup_a_realized1, hiccup_b_realized2)) {
return dom_a} else {
if (squint_core.truth_((() => {
const or__23646__auto__3 = squint_core.not(squint_core.vector_QMARK_(hiccup_a_realized1));
if (or__23646__auto__3) {
return or__23646__auto__3} else {
const or__23646__auto__4 = squint_core.not(squint_core.vector_QMARK_(hiccup_b_realized2));
if (or__23646__auto__4) {
return or__23646__auto__4} else {
return !squint_core._EQ_(squint_core.first(hiccup_a_realized1), squint_core.first(hiccup_b_realized2))};
};

})())) {
const parent5 = dom_a.parentNode;
const parent_ns6 = dom__GT_namespace(parent5);
const new_node7 = hiccup__GT_dom(hiccup_b_realized2, parent_ns6, render_state);
unmount_node_and_children(dom_a);
if (squint_core.truth_((() => {
const c__23562__auto__8 = DocumentFragment;
const x__23563__auto__9 = dom_a;
const ret__23564__auto__10 = (x__23563__auto__9 instanceof c__23562__auto__8);
return ret__23564__auto__10;

})())) {
} else {
dom_a.replaceWith(new_node7)};
return new_node7;
} else {
if ("else") {
patch_attributes(hiccup_a_realized1, hiccup_b_rendered, dom_a);
const a_attrs11 = get_attrs(hiccup_a_realized1);
const b_attrs12 = get_attrs(hiccup_b_rendered);
const a_html13 = squint_core.get_in(a_attrs11, ["dangerouslySetInnerHTML", "__html"]);
const b_html14 = squint_core.get_in(b_attrs12, ["dangerouslySetInnerHTML", "__html"]);
if (!(b_html14 == null)) {
if (!squint_core._EQ_(a_html13, b_html14)) {
dom_a.innerHTML = b_html14}} else {
if (!(a_html13 == null)) {
dom_a.innerHTML = "";
patch_children(hiccup_a_realized1, hiccup_b_rendered, dom_a, render_state)} else {
if ("else") {
patch_children(hiccup_a_realized1, hiccup_b_rendered, dom_a, render_state)} else {
}}};
const a_attrs15 = get_attrs(hiccup_a_realized1);
const b_attrs16 = get_attrs(hiccup_b_rendered);
const b_value17 = squint_core.get(b_attrs16, "value");
if (squint_core.truth_((() => {
const and__23675__auto__18 = squint_core.contains_QMARK_(b_attrs16, "value");
if (squint_core.truth_(and__23675__auto__18)) {
return !squint_core._EQ_(squint_core.get(a_attrs15, "value"), b_value17)} else {
return and__23675__auto__18};

})())) {
if (squint_core.truth_((("SELECT" === dom_a.tagName) && dom_a.multiple))) {
const value_set19 = squint_core.set(b_value17);
for (let G__20 of squint_core.iterable(dom_a.options)) {
const opt21 = G__20;
(opt21["selected"] = squint_core.contains_QMARK_(value_set19, opt21.value))
}} else {
(dom_a["value"] = b_value17)}};
return dom_a;
} else {
return null}}};
};

};
var modify_dom = function (runtime, normalized_component) {
if (squint_core.truth_(squint_core.contains_QMARK_(squint_core.get(squint_core.deref(runtime), "rendering-components"), normalized_component))) {
if (squint_core.truth_(_STAR_watcher_STAR_.val)) {
return queue_watcher_BANG_(_STAR_watcher_STAR_.val);
}} else {
try{
squint_core.swap_BANG_(runtime, squint_core.update, "rendering-components", squint_core.fnil(squint_core.conj, (new Set ([]))), normalized_component);
remove_watchers_for_component(runtime, normalized_component);
const temp__23239__auto__1 = (() => {
const and__23675__auto__2 = runtime;
if (squint_core.truth_(and__23675__auto__2)) {
return runtime_mounted_info(runtime, normalized_component)} else {
return and__23675__auto__2};

})();
if (squint_core.truth_(temp__23239__auto__1)) {
const mounted_info3 = temp__23239__auto__1;
const map__46 = mounted_info3;
const map__47 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__46))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__46))) ? (map__46) : (squint_core.seq_to_map_for_destructuring(map__46)))) : (map__46));
const hiccup8 = squint_core.get(map__47, "hiccup");
const dom9 = squint_core.get(map__47, "dom");
const container10 = squint_core.get(map__47, "container");
const base_namespace11 = squint_core.get(map__47, "base-namespace");
const render_state12 = create_render_state(({"normalized-component": normalized_component, "container": container10, "base-namespace": (() => {
const or__23646__auto__13 = base_namespace11;
if (squint_core.truth_(or__23646__auto__13)) {
return or__23646__auto__13} else {
return dom__GT_namespace(container10)};

})(), "runtime": runtime}));
try{
squint_core.swap_BANG_(render_state12, squint_core.assoc, "positional-key-counter", 0);
const new_hiccup_unrendered14 = with_watcher_bound(normalized_component, render_state12, (function () {
return component__GT_hiccup(normalized_component);

}));
const _15 = squint_core.swap_BANG_(render_state12, squint_core.assoc, "positional-key-counter", 0);
const new_hiccup_rendered16 = fully_render_hiccup(new_hiccup_unrendered14, render_state12);
if (squint_core.truth_((() => {
const and__23675__auto__17 = squint_core.vector_QMARK_(hiccup8);
if (squint_core.truth_(and__23675__auto__17)) {
return ("<>" === squint_core.first(hiccup8))} else {
return and__23675__auto__17};

})())) {
squint_core.swap_BANG_(render_state12, squint_core.assoc, "positional-key-counter", 0);
patch_children(hiccup8, new_hiccup_rendered16, container10, render_state12);
const base_ns18 = squint_core.get(squint_core.deref(render_state12), "base-namespace");
return assoc_runtime_mounted_info_BANG_(runtime, normalized_component, ({"hiccup": new_hiccup_rendered16, "dom": dom9, "container": container10, "base-namespace": base_ns18, "runtime": runtime}));
} else {
const _19 = squint_core.swap_BANG_(render_state12, squint_core.assoc, "positional-key-counter", 0);
const new_dom20 = patch(hiccup8, new_hiccup_rendered16, dom9, render_state12);
const base_ns21 = squint_core.get(squint_core.deref(render_state12), "base-namespace");
assoc_runtime_mounted_info_BANG_(runtime, normalized_component, ({"hiccup": new_hiccup_rendered16, "dom": new_dom20, "container": container10, "base-namespace": base_ns21, "runtime": runtime}));
if (!(dom9 === new_dom20)) {
(container10["innerHTML"] = "");
return container10.appendChild(new_dom20);
};
};
}
finally{
squint_core.swap_BANG_(render_state12, squint_core.assoc, "active", false)}
;
};
}
finally{
squint_core.swap_BANG_(runtime, squint_core.update, "rendering-components", squint_core.disj, normalized_component)}
};

};
var notify_watchers = function (watchers) {
for (let G__1 of squint_core.iterable(squint_core.vals(squint_core.deref(watchers)))) {
const watcher2 = G__1;
if (squint_core.truth_(watcher2)) {
if (squint_core.truth_(should_defer_watcher_QMARK_(watcher2))) {
queue_watcher_BANG_(watcher2)} else {
run_watcher_now(watcher2)}}
}
return null;

};
var add_modify_dom_watcher_on_ratom_deref = function (normalized_component, render_state) {
return with_watcher_bound(normalized_component, render_state, (function () {
const reagent_render1 = squint_core.get(squint_core.first(normalized_component), "reagent-render");
const params2 = squint_core.rest(normalized_component);
const hiccup3 = squint_core.apply(reagent_render1, params2);
const base_ns4 = squint_core.get(squint_core.deref(render_state), "base-namespace");
const dom5 = hiccup__GT_dom(hiccup3, base_ns4, render_state);
return [hiccup3, dom5];

}));

};
var unmount_components = function (container) {
const temp__23239__auto__1 = squint_core.get(squint_core.deref(roots), container);
if (squint_core.truth_(temp__23239__auto__1)) {
const map__24 = temp__23239__auto__1;
const map__25 = ((squint_core.truth_(squint_core.sequential_QMARK_(map__24))) ? (((squint_core.truth_(squint_core.vector_QMARK_(map__24))) ? (map__24) : (squint_core.seq_to_map_for_destructuring(map__24)))) : (map__24));
const runtime6 = squint_core.get(map__25, "runtime");
if (squint_core.truth_(runtime6)) {
remove_all_runtime_watchers_BANG_(runtime6)};
squint_core.swap_BANG_(roots, squint_core.dissoc, container)};
for (let G__7 of squint_core.iterable(squint_core.vec(container["childNodes"]))) {
const child8 = G__7;
remove_node_and_unmount_BANG_(child8)
}
return null;

};
var do_render = function (normalized_component, container, render_state) {
unmount_components(container);
squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", 0);
try{
const runtime4 = render_state_runtime(render_state);
const base_ns5 = squint_core.get(squint_core.deref(render_state), "base-namespace");
const vec__16 = add_modify_dom_watcher_on_ratom_deref(normalized_component, render_state);
const hiccup7 = squint_core.nth(vec__16, 0, null);
const dom8 = squint_core.nth(vec__16, 1, null);
const _9 = squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", 0);
const hiccup_rendered10 = fully_render_hiccup(hiccup7, render_state);
container.appendChild(dom8);
assoc_runtime_mounted_info_BANG_(runtime4, normalized_component, ({"hiccup": hiccup_rendered10, "dom": dom8, "container": container, "base-namespace": base_ns5, "runtime": runtime4}));
if (squint_core.truth_(container)) {
return squint_core.swap_BANG_(roots, squint_core.assoc, container, ({"container": container, "component": normalized_component, "runtime": runtime4}));
};
}
finally{
squint_core.swap_BANG_(render_state, squint_core.assoc, "active", false)}
;

};
var RAtom = function (base, watchers, cursors) {
this.base = base;
this.watchers = watchers;
this.cursors = cursors;

};
(RAtom.prototype[(squint_core.IDeref["__sym"])] = true);
let f__22940__auto__36 = (function (this$) {
const self__ = this;;
ensure_watcher_registered_BANG_(this$, self__.watchers);
return squint_core.deref(self__.base);

});
(RAtom.prototype[squint_core.IDeref__deref] = f__22940__auto__36);
(RAtom.prototype[(squint_core.IReset["__sym"])] = true);
let f__22940__auto__37 = (function (_this, new_val) {
const self__ = this;;
const res1 = squint_core.reset_BANG_(self__.base, new_val);
notify_watchers(self__.watchers);
for (let G__2 of squint_core.iterable(squint_core.deref(self__.cursors))) {
const c3 = G__2;
notify_watchers(c3["watchers"])
};
return res1;

});
(RAtom.prototype[squint_core.IReset__reset_BANG_] = f__22940__auto__37);
(RAtom.prototype[(squint_core.ISwap["__sym"])] = true);
let f__22940__auto__38 = /* @__PURE__ */ (() => {
const impl424 = (function (this$, f) {
const self__ = this;;
return squint_core._reset_BANG_(this$, f(squint_core.deref(self__.base)));

});
const impl435 = (function (this$, f, a) {
const self__ = this;;
return squint_core._reset_BANG_(this$, f(squint_core.deref(self__.base), a));

});
const impl446 = (function (this$, f, a, b) {
const self__ = this;;
return squint_core._reset_BANG_(this$, f(squint_core.deref(self__.base), a, b));

});
const impl457 = (function (this$, f, a, b, xs) {
const self__ = this;;
return squint_core._reset_BANG_(this$, squint_core.apply(f, squint_core.deref(self__.base), a, b, xs));

});
const f39 = (function (...args40) {
const self468 = this;
const G__479 = args40.length;
switch (G__479) {case 2:
return impl424.call(self468, args40[0], args40[1]);

break;
case 3:
return impl435.call(self468, args40[0], args40[1], args40[2]);

break;
case 4:
return impl446.call(self468, args40[0], args40[1], args40[2], args40[3]);

break;
case 5:
return impl457.call(self468, args40[0], args40[1], args40[2], args40[3], args40[4]);

break;
default:
throw (new Error(`${"Invalid arity: "}${args40.length??''}`))};

});
return f39;

})();
(RAtom.prototype[squint_core.ISwap__swap_BANG_] = f__22940__auto__38);
(RAtom.prototype[(squint_core.IWatchable["__sym"])] = true);
let f__22940__auto__48 = (function (this$, k, f) {
const self__ = this;;
squint_core.add_watch(self__.base, k, (function (k, _, o, n) {
return f(k, this$, o, n);

}));
return this$;

});
(RAtom.prototype[squint_core.IWatchable__add_watch] = f__22940__auto__48);
let f__22940__auto__49 = (function (_this, k) {
const self__ = this;;
return squint_core.remove_watch(self__.base, k);

});
(RAtom.prototype[squint_core.IWatchable__remove_watch] = f__22940__auto__49);
let f__22940__auto__50 = (function (this$, _oldv, _newv) {
const self__ = this;;
notify_watchers(self__.watchers);
return this$;

});
(RAtom.prototype[squint_core.IWatchable__notify_watches] = f__22940__auto__50);
var __GT_RAtom = function (base, watchers, cursors) {
return (new RAtom(base, watchers, cursors));

};
RAtom;
var ratom = function (initial_value) {
return __GT_RAtom(core_atom(initial_value), core_atom(empty_js_map()), core_atom((new Set ([]))));

};
var Cursor = function (the_ratom, path, watchers) {
this.the_ratom = the_ratom;
this.path = path;
this.watchers = watchers;

};
(Cursor.prototype[(squint_core.IDeref["__sym"])] = true);
let f__22940__auto__51 = (function (this$) {
const self__ = this;;
ensure_watcher_registered_BANG_(this$, self__.watchers);
const old_watcher1 = _STAR_watcher_STAR_.val;
try{
_STAR_watcher_STAR_.val = null;
return squint_core.get_in(squint_core.deref(self__.the_ratom), self__.path);
}
finally{
_STAR_watcher_STAR_.val = old_watcher1}
;

});
(Cursor.prototype[squint_core.IDeref__deref] = f__22940__auto__51);
(Cursor.prototype[(squint_core.ISwap["__sym"])] = true);
let f__22940__auto__52 = /* @__PURE__ */ (() => {
const impl562 = (function (_this, f) {
const self__ = this;;
return squint_core.swap_BANG_(self__.the_ratom, (function (state) {
return squint_core.assoc_in(state, self__.path, f(squint_core.get_in(state, self__.path)));

}));

});
const impl573 = (function (_this, f, a) {
const self__ = this;;
return squint_core.swap_BANG_(self__.the_ratom, (function (state) {
return squint_core.assoc_in(state, self__.path, f(squint_core.get_in(state, self__.path), a));

}));

});
const impl584 = (function (_this, f, a, b) {
const self__ = this;;
return squint_core.swap_BANG_(self__.the_ratom, (function (state) {
return squint_core.assoc_in(state, self__.path, f(squint_core.get_in(state, self__.path), a, b));

}));

});
const impl595 = (function (_this, f, a, b, xs) {
const self__ = this;;
return squint_core.swap_BANG_(self__.the_ratom, (function (state) {
return squint_core.assoc_in(state, self__.path, squint_core.apply(f, squint_core.get_in(state, self__.path), a, b, xs));

}));

});
const f53 = (function (...args54) {
const self606 = this;
const G__617 = args54.length;
switch (G__617) {case 2:
return impl562.call(self606, args54[0], args54[1]);

break;
case 3:
return impl573.call(self606, args54[0], args54[1], args54[2]);

break;
case 4:
return impl584.call(self606, args54[0], args54[1], args54[2], args54[3]);

break;
case 5:
return impl595.call(self606, args54[0], args54[1], args54[2], args54[3], args54[4]);

break;
default:
throw (new Error(`${"Invalid arity: "}${args54.length??''}`))};

});
return f53;

})();
(Cursor.prototype[squint_core.ISwap__swap_BANG_] = f__22940__auto__52);
(Cursor.prototype[(squint_core.IReset["__sym"])] = true);
let f__22940__auto__62 = (function (this$, new_val) {
const self__ = this;;
return squint_core._swap_BANG_(this$, squint_core.constantly(new_val));

});
(Cursor.prototype[squint_core.IReset__reset_BANG_] = f__22940__auto__62);
var __GT_Cursor = function (the_ratom, path, watchers) {
return (new Cursor(the_ratom, path, watchers));

};
Cursor;
var cursor = function (the_ratom, path) {
const cursors1 = the_ratom.cursors;
const found_cursor2 = squint_core.some((function (c) {
if (squint_core._EQ_(path, c["path"])) {
return c;
};

}), squint_core.deref(cursors1));
if ((found_cursor2 == null)) {
const this_cursor3 = __GT_Cursor(the_ratom, path, core_atom(empty_js_map()));
squint_core.swap_BANG_(cursors1, squint_core.conj, this_cursor3);
return this_cursor3;
} else {
return found_cursor2};

};
var Reaction = function (ra) {
this.ra = ra;

};
(Reaction.prototype[(squint_core.IDeref["__sym"])] = true);
let f__22940__auto__63 = (function (_this) {
const self__ = this;;
return squint_core.deref(self__.ra);

});
(Reaction.prototype[squint_core.IDeref__deref] = f__22940__auto__63);
(Reaction.prototype[(squint_core.ISwap["__sym"])] = true);
let f__22940__auto__64 = (function (_this, _f) {
const self__ = this;;
throw (new Error("Reactions are readonly"));

});
(Reaction.prototype[squint_core.ISwap__swap_BANG_] = f__22940__auto__64);
(Reaction.prototype[(squint_core.IReset["__sym"])] = true);
let f__22940__auto__65 = (function (_this, _v) {
const self__ = this;;
throw (new Error("Reactions are readonly"));

});
(Reaction.prototype[squint_core.IReset__reset_BANG_] = f__22940__auto__65);
var __GT_Reaction = function (ra) {
return (new Reaction(ra));

};
Reaction;
var reaction = /* @__PURE__ */ (() => {
const impl691 = (function (f, params) {
const ra2 = ratom(null);
const watcher3 = (function () {
return squint_core.reset_BANG_(ra2, squint_core.apply(f, params));

});
const old_watcher4 = _STAR_watcher_STAR_.val;
try{
_STAR_watcher_STAR_.val = watcher3;
watcher3();
return __GT_Reaction(ra2);
}
finally{
_STAR_watcher_STAR_.val = old_watcher4}
;

});
const f66 = (function (arg67, ...rest68) {
const self__23452__auto__5 = this;
return impl691.call(self__23452__auto__5, arg67, (((rest68.length === 0)) ? (null) : (rest68)));

});
(f66["squint$lang$variadic"] = impl691);
return f66;

})();
var render = function (component, container) {
const runtime1 = core_atom(({"runtime-id": `runtime-${squint_core.random_uuid()??''}`, "component-instances": empty_js_map(), "pending-watchers": [], "watcher-flush-scheduled?": false, "mounted-components": empty_js_map(), "subscriptions": empty_js_map(), "rendering-components": (new Set ([]))}));
const base_ns2 = dom__GT_namespace(container);
const render_state3 = create_render_state(({"container": container, "base-namespace": base_ns2, "runtime": runtime1}));
const normalized4 = normalize_component(component, render_state3);
squint_core.swap_BANG_(render_state3, squint_core.assoc, "normalized-component", normalized4);
return do_render(normalized4, container, render_state3);

};
var atom = ratom;

export { roots, reaction, default_namespace, _STAR_watcher_STAR_, __GT_Reaction, atom, cursor, uri__GT_namespace, render, __GT_Cursor, namespaces, __GT_RAtom, entry_tag__GT_namespace }
