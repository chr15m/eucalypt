import * as squint_core from 'squint-cljs/core.js';
import * as squint from 'squint-cljs/core.js';
var core_atom = squint.atom;
var empty_js_map = function () {
return new Map();

};
var default_namespace = "html";
var namespaces = ({ "html": ({ "uri": "http://www.w3.org/1999/xhtml" }), "svg": ({ "uri": "http://www.w3.org/2000/svg", "entry-tags": new Set(["svg"]), "boundary-tags": new Set(["foreignObject"]) }), "math": ({ "uri": "http://www.w3.org/1998/Math/MathML", "entry-tags": new Set(["math"]), "boundary-tags": new Set(["annotation-xml"]) }) });
var entry_tag__GT_namespace = squint_core.into(({  }), squint_core.mapcat((function (p__19) {
const vec__15 = p__19;
const ns6 = squint_core.nth(vec__15, 0, null);
const map__47 = squint_core.nth(vec__15, 1, null);
const entry_tags8 = squint_core.get(map__47, "entry-tags");
return squint_core.map((function (tag) {
return [tag, ns6];

}), entry_tags8);

}), namespaces));
var uri__GT_namespace = squint_core.into(({  }), squint_core.keep((function (p__20) {
const vec__15 = p__20;
const ns6 = squint_core.nth(vec__15, 0, null);
const map__47 = squint_core.nth(vec__15, 1, null);
const uri8 = squint_core.get(map__47, "uri");
if (squint_core.truth_(uri8)) {
return [uri8, ns6];
};

}), namespaces));
var _STAR_watcher_STAR_ = null;
var roots = core_atom(empty_js_map());
var namespace_uri = function (ns_key) {
const or__23141__auto__1 = squint_core.get_in(namespaces, [ns_key, "uri"]);
if (squint_core.truth_(or__23141__auto__1)) {
return or__23141__auto__1} else {
return squint_core.get_in(namespaces, [default_namespace, "uri"])};

};
var normalize_namespace = function (uri) {
const candidate1 = (() => {
const or__23141__auto__2 = uri;
if (squint_core.truth_(or__23141__auto__2)) {
return or__23141__auto__2} else {
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
const boundary_tags3 = squint_core.get_in(namespaces, [current_key2, "boundary-tags"], new Set([]));
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
if (squint_core.truth_(squint_core.some_QMARK_(dom))) {
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
const temp__22756__auto__1 = squint_core.get(meta_STAR_(watcher), "runtime");
if (squint_core.truth_(temp__22756__auto__1)) {
const runtime2 = temp__22756__auto__1;
return squint_core.swap_BANG_(runtime2, squint_core.update, "pending-watchers", (function (queue) {
const existing3 = (() => {
const or__23141__auto__4 = queue;
if (squint_core.truth_(or__23141__auto__4)) {
return or__23141__auto__4} else {
return []};

})();
return squint_core.into([], squint_core.remove((function (_PERCENT_1) {
return squint_core._EQ_(watcher, _PERCENT_1);

}), existing3));

}));
};

};
var watcher_entry_key = function (watcher) {
const or__23141__auto__1 = squint_core.get(meta_STAR_(watcher), "normalized-component");
if (squint_core.truth_(or__23141__auto__1)) {
return or__23141__auto__1} else {
return watcher};

};
var register_watcher_with_host_BANG_ = function (host, watchers_atom, watcher) {
const meta_info1 = meta_STAR_(watcher);
const runtime2 = squint_core.get(meta_info1, "runtime");
const component_key3 = squint_core.get(meta_info1, "normalized-component");
if (squint_core.truth_((() => {
const and__23153__auto__4 = runtime2;
if (squint_core.truth_(and__23153__auto__4)) {
const and__23153__auto__5 = component_key3;
if (squint_core.truth_(and__23153__auto__5)) {
return host} else {
return and__23153__auto__5};
} else {
return and__23153__auto__4};

})())) {
return squint_core.swap_BANG_(runtime2, (function (state) {
const existing6 = squint_core.get_in(state, ["subscriptions", component_key3, host]);
if (squint_core.truth_(squint_core._EQ_(watcher, squint_core.get(existing6, "watcher")))) {
return state} else {
const subs7 = (() => {
const or__23141__auto__8 = squint_core.get(state, "subscriptions");
if (squint_core.truth_(or__23141__auto__8)) {
return or__23141__auto__8} else {
return empty_js_map()};

})();
const component_map9 = (() => {
const or__23141__auto__10 = squint_core.get(subs7, component_key3);
if (squint_core.truth_(or__23141__auto__10)) {
return or__23141__auto__10} else {
return empty_js_map()};

})();
const entry11 = ({ "host": host, "watchers-atom": watchers_atom, "watcher": watcher });
const new_component_map12 = squint_core.assoc(component_map9, host, entry11);
const new_subs13 = squint_core.assoc(subs7, component_key3, new_component_map12);
return squint_core.assoc(state, "subscriptions", new_subs13);
};

}));
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
const or__23141__auto__1 = instances;
if (squint_core.truth_(or__23141__auto__1)) {
return or__23141__auto__1} else {
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
const or__23141__auto__1 = components;
if (squint_core.truth_(or__23141__auto__1)) {
return or__23141__auto__1} else {
return empty_js_map()};

})(), normalized_component, info);

}));
};

};
var create_render_state = function (p__21) {
const map__12 = p__21;
const normalized_component3 = squint_core.get(map__12, "normalized-component");
const container4 = squint_core.get(map__12, "container");
const base_namespace5 = squint_core.get(map__12, "base-namespace");
const runtime6 = squint_core.get(map__12, "runtime");
const state7 = ({ "active": true, "positional-key-counter": 0, "base-namespace": normalize_namespace(base_namespace5) });
const state8 = (() => {
const G__229 = state7;
const G__2210 = ((squint_core.truth_(normalized_component3)) ? (squint_core.assoc(G__229, "normalized-component", normalized_component3)) : (G__229));
const G__2211 = ((squint_core.truth_(container4)) ? (squint_core.assoc(G__2210, "container", container4)) : (G__2210));
if (squint_core.truth_(runtime6)) {
return squint_core.assoc(G__2211, "runtime", runtime6)} else {
return G__2211};

})();
return core_atom(state8);

};
var next_positional_key_BANG_ = function (render_state) {
const next_val1 = (squint_core.get(squint_core.deref(render_state), "positional-key-counter") + 1);
squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", next_val1);
return next_val1;

};
var run_watcher_now = function (watcher) {
const old_watcher1 = _STAR_watcher_STAR_;
return (() => {
try{
_STAR_watcher_STAR_ = watcher;
return watcher();
}
finally{
_STAR_watcher_STAR_ = old_watcher1}

})();

};
var flush_queued_watchers = function (runtime) {
const queued1 = squint_core.get(squint_core.deref(runtime), "pending-watchers");
squint_core.swap_BANG_(runtime, (function (state) {
return squint_core.assoc(squint_core.assoc(state, "pending-watchers", []), "watcher-flush-scheduled?", false);

}));
for (let G__2 of squint_core.iterable(queued1)) {
const watcher3 = G__2;
run_watcher_now(watcher3)
}return null;

};
var schedule_watcher_flush_BANG_ = function (runtime) {
if (squint_core.truth_((() => {
const and__23153__auto__1 = runtime;
if (squint_core.truth_(and__23153__auto__1)) {
return squint_core.not(squint_core.get(squint_core.deref(runtime), "watcher-flush-scheduled?"))} else {
return and__23153__auto__1};

})())) {
squint_core.swap_BANG_(runtime, squint_core.assoc, "watcher-flush-scheduled?", true);
const flush_fn2 = (function () {
return flush_queued_watchers(runtime);

});
if (squint_core.truth_(squint_core.some_QMARK_(globalThis.queueMicrotask))) {
return globalThis.queueMicrotask(flush_fn2)} else {
return setTimeout(flush_fn2, 0)};
};

};
var queue_watcher_BANG_ = function (watcher) {
const temp__22705__auto__1 = squint_core.get(meta_STAR_(watcher), "runtime");
if (squint_core.truth_(temp__22705__auto__1)) {
const runtime2 = temp__22705__auto__1;
squint_core.swap_BANG_(runtime2, squint_core.update, "pending-watchers", squint_core.fnil(squint_core.conj, []), watcher);
return schedule_watcher_flush_BANG_(runtime2);
} else {
return run_watcher_now(watcher)};

};
var should_defer_watcher_QMARK_ = function (watcher) {
const meta_info1 = meta_STAR_(watcher);
const defer_fn2 = (() => {
const and__23153__auto__3 = meta_info1;
if (squint_core.truth_(and__23153__auto__3)) {
return squint_core.get(meta_info1, "should-defer?")} else {
return and__23153__auto__3};

})();
return squint_core.boolean$((() => {
const and__23153__auto__4 = squint_core.fn_QMARK_(defer_fn2);
if (squint_core.truth_(and__23153__auto__4)) {
return defer_fn2()} else {
return and__23153__auto__4};

})());

};
var with_watcher_bound = function (normalized_component, render_state, f) {
const old_watcher1 = _STAR_watcher_STAR_;
const runtime2 = render_state_runtime(render_state);
const watcher_fn3 = with_meta_STAR_((function () {
return modify_dom(runtime2, normalized_component);

}), ({ "normalized-component": normalized_component, "should-defer?": (function () {
return squint_core.boolean$(squint_core.get(squint_core.deref(render_state), "active"));

}), "runtime": runtime2 }));
return (() => {
try{
_STAR_watcher_STAR_ = watcher_fn3;
return f();
}
finally{
_STAR_watcher_STAR_ = old_watcher1}

})();

};
var remove_watchers_for_component = function (runtime, normalized_component) {
const runtime_state1 = ((squint_core.truth_(runtime)) ? (squint_core.deref(runtime)) : (null));
const subscriptions2 = squint_core.get_in(runtime_state1, ["subscriptions", normalized_component]);
if (squint_core.truth_((() => {
const and__23153__auto__3 = runtime;
if (squint_core.truth_(and__23153__auto__3)) {
return normalized_component} else {
return and__23153__auto__3};

})())) {
if (squint_core.truth_(squint_core.seq(subscriptions2))) {
for (let G__4 of squint_core.iterable(squint_core.vals(subscriptions2))) {
const map__56 = G__4;
const watchers_atom7 = squint_core.get(map__56, "watchers-atom");
const watcher8 = squint_core.get(map__56, "watcher");
remove_watcher_from_runtime_queue_BANG_(watcher8);
if (squint_core.truth_((() => {
const and__23153__auto__9 = watchers_atom7;
if (squint_core.truth_(and__23153__auto__9)) {
return watcher8} else {
return and__23153__auto__9};

})())) {
const key10 = watcher_entry_key(watcher8);
squint_core.swap_BANG_(watchers_atom7, (function (state) {
return squint_core.dissoc((() => {
const or__23141__auto__11 = state;
if (squint_core.truth_(or__23141__auto__11)) {
return or__23141__auto__11} else {
return empty_js_map()};

})(), key10);

}))}
}}};
squint_core.swap_BANG_(runtime, (function (state) {
const subs12 = (() => {
const or__23141__auto__13 = squint_core.get(state, "subscriptions");
if (squint_core.truth_(or__23141__auto__13)) {
return or__23141__auto__13} else {
return empty_js_map()};

})();
const new_subs14 = squint_core.dissoc(subs12, normalized_component);
return squint_core.assoc(state, "subscriptions", new_subs14);

}));
return null;

};
var remove_all_runtime_watchers_BANG_ = function (runtime) {
if (squint_core.truth_(runtime)) {
const components1 = squint_core.keys((() => {
const or__23141__auto__2 = squint_core.get(squint_core.deref(runtime), "subscriptions");
if (squint_core.truth_(or__23141__auto__2)) {
return or__23141__auto__2} else {
return empty_js_map()};

})());
for (let G__3 of squint_core.iterable(components1)) {
const component4 = G__3;
remove_watchers_for_component(runtime, component4)
}return null;
};

};
var style_map__GT_css_str = function (style_map) {
return squint_core.apply(squint_core.str, squint_core.map((function (p__23) {
const vec__14 = p__23;
const k5 = squint_core.nth(vec__14, 0, null);
const v6 = squint_core.nth(vec__14, 1, null);
return squint_core.str(k5, ":", v6, ";");

}), style_map));

};
var get_event_name = function (k, tag_name) {
if (squint_core.truth_((() => {
const and__23153__auto__1 = ("on-change" === k);
if (squint_core.truth_(and__23153__auto__1)) {
return squint_core.get(new Set(["INPUT", "TEXTAREA"]), tag_name)} else {
return and__23153__auto__1};

})())) {
return "oninput"} else {
if (squint_core.truth_(("on-double-click" === k))) {
return "ondblclick"} else {
if ("else") {
return k.replaceAll("-", "")} else {
return null}}};

};
var set_attributes_BANG_ = function (element, attrs) {
for (let G__1 of squint_core.iterable(attrs)) {
const vec__25 = G__1;
const k6 = squint_core.nth(vec__25, 0, null);
const v7 = squint_core.nth(vec__25, 1, null);
if (squint_core.truth_(("xmlns" === k6))) {
} else {
if (squint_core.truth_(("ref" === k6))) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
(element["---ref-fn"] = v7);
v7(element)}} else {
if (squint_core.truth_(k6.startsWith("on-"))) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
const event_name8 = get_event_name(k6, element.tagName);
(element[event_name8] = v7)}} else {
if (squint_core.truth_(("style" === k6))) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
const css9 = style_map__GT_css_str(v7);
if (squint_core.truth_(squint_core.seq(css9))) {
element.setAttribute("style", css9)} else {
element.removeAttribute("style")}}} else {
if (squint_core.truth_(("class" === k6))) {
const class_val10 = ((squint_core.truth_((() => {
const and__23153__auto__11 = squint_core.sequential_QMARK_(v7);
if (squint_core.truth_(and__23153__auto__11)) {
return squint_core.not(squint_core.string_QMARK_(v7))} else {
return and__23153__auto__11};

})())) ? (squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, v7)).join(" ")) : (v7));
if (squint_core.truth_((() => {
const or__23141__auto__12 = (class_val10 == null);
if (or__23141__auto__12) {
return or__23141__auto__12} else {
return ("" === class_val10)};

})())) {
element.removeAttribute("class")} else {
element.setAttribute("class", class_val10)}} else {
if (squint_core.truth_((() => {
const or__23141__auto__13 = ("checked" === k6);
if (squint_core.truth_(or__23141__auto__13)) {
return or__23141__auto__13} else {
return ("selected" === k6)};

})())) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
(element[k6] = v7)}} else {
if ("else") {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
element.setAttributeNS(null, k6, v7)}} else {
}}}}}}}
}return null;

};
var parse_tag = function (tag) {
const tag_str10 = squint_core.str(tag);
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
return ({ "tag-name": tag_name20, "id": id25, "classes": ((squint_core.truth_(squint_core.seq(all_classes27))) ? (all_classes27) : (null)) });

};
var parse_hiccup = function (hiccup) {
const vec__15 = hiccup;
const seq__26 = squint_core.seq(vec__15);
const first__37 = squint_core.first(seq__26);
const seq__28 = squint_core.next(seq__26);
const tag_keyword9 = first__37;
const content10 = seq__28;
const map__411 = parse_tag(tag_keyword9);
const tag_name12 = squint_core.get(map__411, "tag-name");
const id13 = squint_core.get(map__411, "id");
const classes14 = squint_core.get(map__411, "classes");
const attrs_from_hiccup15 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content10)))) ? (squint_core.first(content10)) : (({  })));
const final_id16 = (() => {
const or__23141__auto__17 = squint_core.get(attrs_from_hiccup15, "id");
if (squint_core.truth_(or__23141__auto__17)) {
return or__23141__auto__17} else {
return id13};

})();
const class_from_hiccup18 = squint_core.get(attrs_from_hiccup15, "class");
const all_classes19 = (() => {
const tag_classes20 = (() => {
const or__23141__auto__21 = classes14;
if (squint_core.truth_(or__23141__auto__21)) {
return or__23141__auto__21} else {
return []};

})();
const attr_classes22 = (((class_from_hiccup18 == null)) ? ([]) : (((squint_core.truth_(squint_core.string_QMARK_(class_from_hiccup18))) ? ([class_from_hiccup18]) : (((squint_core.truth_((() => {
const and__23153__auto__23 = squint_core.sequential_QMARK_(class_from_hiccup18);
if (squint_core.truth_(and__23153__auto__23)) {
return squint_core.not(squint_core.string_QMARK_(class_from_hiccup18))} else {
return and__23153__auto__23};

})())) ? (squint_core.vec(class_from_hiccup18)) : ((("else") ? ([class_from_hiccup18]) : (null))))))));
const combined24 = squint_core.vec(squint_core.concat(tag_classes20, attr_classes22));
if (squint_core.truth_(squint_core.seq(combined24))) {
return combined24;
};

})();
const attrs_with_id25 = ((squint_core.truth_(final_id16)) ? (squint_core.assoc(attrs_from_hiccup15, "id", final_id16)) : (attrs_from_hiccup15));
const final_attrs26 = ((squint_core.truth_(squint_core.some_QMARK_(all_classes19))) ? (squint_core.assoc(attrs_with_id25, "class", all_classes19)) : (squint_core.dissoc(attrs_with_id25, "class")));
const final_content27 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content10)))) ? (squint_core.rest(content10)) : (content10));
return ({ "tag-name": tag_name12, "attrs": final_attrs26, "content": final_content27 });

};
var create_element = function (hiccup, current_ns, render_state) {
const map__12 = parse_hiccup(hiccup);
const tag_name3 = squint_core.get(map__12, "tag-name");
const attrs4 = squint_core.get(map__12, "attrs");
const content5 = squint_core.get(map__12, "content");
const value6 = squint_core.get(attrs4, "value");
const attrs_without_value7 = squint_core.dissoc(attrs4, "value");
const new_ns8 = next_namespace(normalize_namespace(current_ns), tag_name3);
const element9 = document.createElementNS(new_ns8, tag_name3);
set_attributes_BANG_(element9, attrs_without_value7);
for (let G__10 of squint_core.iterable(content5)) {
const child11 = G__10;
const temp__22756__auto__12 = hiccup__GT_dom(child11, new_ns8, render_state);
if (squint_core.truth_(temp__22756__auto__12)) {
const child_node13 = temp__22756__auto__12;
element9.appendChild(child_node13)}
};
if (squint_core.truth_(squint_core.some_QMARK_(value6))) {
if (squint_core.truth_((() => {
const and__23153__auto__14 = ("SELECT" === element9.tagName);
if (squint_core.truth_(and__23153__auto__14)) {
return element9.multiple} else {
return and__23153__auto__14};

})())) {
const value_set15 = squint_core.set(value6);
for (let G__16 of squint_core.iterable(element9.options)) {
const opt17 = G__16;
(opt17["selected"] = squint_core.contains_QMARK_(value_set15, opt17.value))
}} else {
(element9["value"] = value6)}};
return element9;

};
var normalize_component = function (component, render_state) {
if (squint_core.truth_(squint_core.vector_QMARK_(component))) {
const first_element1 = component[0];
const params2 = squint_core.subvec(component, 1);
if (squint_core.truth_(squint_core.fn_QMARK_(first_element1))) {
const a_fn3 = first_element1;
const params_vec4 = squint_core.vec(params2);
const component_meta5 = squint_core.meta(component);
const runtime6 = render_state_runtime(render_state);
const component_cache7 = runtime_component_cache(runtime6);
const fn_cache8 = ((squint_core.truth_(component_cache7)) ? (squint_core.get(component_cache7, a_fn3)) : (null));
const cached_form1_instance9 = ((squint_core.truth_(fn_cache8)) ? (squint_core.get(fn_cache8, "form-1-instance")) : (null));
const instance_key10 = ((squint_core.truth_(squint_core.contains_QMARK_(component_meta5, "key"))) ? (squint_core.get(component_meta5, "key")) : (((squint_core.truth_(render_state)) ? (next_positional_key_BANG_(render_state)) : (squint_core.random_uuid()))));
const cached_form2_instance11 = ((squint_core.truth_(fn_cache8)) ? (squint_core.get(fn_cache8, instance_key10)) : (null));
if (squint_core.truth_(cached_form2_instance11)) {
return squint_core.into([squint_core.get(cached_form2_instance11, "instance")], params_vec4)} else {
if (squint_core.truth_(cached_form1_instance9)) {
return squint_core.into([squint_core.get(cached_form1_instance9, "instance")], params_vec4)} else {
if ("else") {
const func_or_hiccup12 = squint_core.apply(a_fn3, params_vec4);
if (squint_core.truth_(squint_core.fn_QMARK_(func_or_hiccup12))) {
const closure13 = func_or_hiccup12;
const instance14 = ({ "reagent-render": closure13 });
const result15 = squint_core.into([instance14], params_vec4);
update_component_cache_BANG_(runtime6, (function (cache) {
const fn_cache16 = (() => {
const or__23141__auto__17 = squint_core.get(cache, a_fn3);
if (squint_core.truth_(or__23141__auto__17)) {
return or__23141__auto__17} else {
return empty_js_map()};

})();
const new_fn_cache18 = squint_core.assoc(fn_cache16, instance_key10, ({ "type": "form-2", "instance": instance14 }));
return squint_core.assoc(cache, a_fn3, new_fn_cache18);

}));
return result15;
} else {
const instance19 = ({ "reagent-render": a_fn3 });
const result20 = squint_core.into([instance19], params_vec4);
update_component_cache_BANG_(runtime6, (function (cache) {
const fn_cache21 = (() => {
const or__23141__auto__22 = squint_core.get(cache, a_fn3);
if (squint_core.truth_(or__23141__auto__22)) {
return or__23141__auto__22} else {
return empty_js_map()};

})();
const new_fn_cache23 = squint_core.assoc(fn_cache21, "form-1-instance", ({ "type": "form-1", "instance": instance19 }));
return squint_core.assoc(cache, a_fn3, new_fn_cache23);

}));
return result20;
};
} else {
return null}}};
} else {
if (squint_core.truth_(squint_core.string_QMARK_(first_element1))) {
return squint_core.into([({ "reagent-render": (function () {
return component;

}) })], params2)} else {
if (squint_core.truth_(squint_core.map_QMARK_(first_element1))) {
const component_as_map24 = first_element1;
const render_fn25 = squint_core.get(component_as_map24, "reagent-render");
const comp_with_lifecycle26 = ({ "reagent-render": render_fn25 });
return squint_core.into([comp_with_lifecycle26], params2);
} else {
return null}}};
};

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
var hiccup__GT_dom = (() => {
const f24 = (function (...args25) {
const G__261 = args25.length;
switch (G__261) {case 2:
return f24.cljs$core$IFn$_invoke$arity$2(args25[0], args25[1]);

break;
case 3:
return f24.cljs$core$IFn$_invoke$arity$3(args25[0], args25[1], args25[2]);

break;
default:
throw new Error(squint_core.str("Invalid arity: ", args25.length))};

});
f24.cljs$core$IFn$_invoke$arity$2 = (function (hiccup, render_state) {
return hiccup__GT_dom(hiccup, namespace_uri(default_namespace), render_state);

});
f24.cljs$core$IFn$_invoke$arity$3 = (function (hiccup, current_ns, render_state) {
const result3 = ((squint_core.truth_((() => {
const or__23141__auto__4 = squint_core.string_QMARK_(hiccup);
if (squint_core.truth_(or__23141__auto__4)) {
return or__23141__auto__4} else {
return squint_core.number_QMARK_(hiccup)};

})())) ? (document.createTextNode(squint_core.str(hiccup))) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup))) ? ((() => {
const tag5 = hiccup[0];
if (squint_core.truth_(squint_core.fn_QMARK_(tag5))) {
return hiccup__GT_dom(component__GT_hiccup(normalize_component(hiccup, render_state)), current_ns, render_state)} else {
if (squint_core.truth_(("<>" === tag5))) {
const fragment6 = document.createDocumentFragment();
for (let G__7 of squint_core.iterable(squint_core.rest(hiccup))) {
const child8 = G__7;
const temp__22756__auto__9 = hiccup__GT_dom(child8, current_ns, render_state);
if (squint_core.truth_(temp__22756__auto__9)) {
const child_node10 = temp__22756__auto__9;
fragment6.appendChild(child_node10)}
};
return fragment6;
} else {
if ("else") {
return create_element(hiccup, current_ns, render_state)} else {
return null}}};

})()) : (((squint_core.truth_(squint_core.seq_QMARK_(hiccup))) ? ((() => {
const fragment11 = document.createDocumentFragment();
for (let G__12 of squint_core.iterable(hiccup)) {
const item13 = G__12;
const item_with_meta14 = ((squint_core.truth_((() => {
const and__23153__auto__15 = squint_core.vector_QMARK_(item13);
if (squint_core.truth_(and__23153__auto__15)) {
return squint_core.meta(item13)} else {
return and__23153__auto__15};

})())) ? (squint_core.with_meta(item13, squint_core.meta(item13))) : (item13));
const temp__22756__auto__16 = hiccup__GT_dom(item_with_meta14, current_ns, render_state);
if (squint_core.truth_(temp__22756__auto__16)) {
const child_node17 = temp__22756__auto__16;
fragment11.appendChild(child_node17)}
};
return fragment11;

})()) : (((squint_core.truth_((() => {
const or__23141__auto__18 = (hiccup == null);
if (or__23141__auto__18) {
return or__23141__auto__18} else {
return squint_core.boolean_QMARK_(hiccup)};

})())) ? (null) : ((("else") ? (document.createTextNode(squint_core.str(hiccup))) : (null))))))))));
return result3;

});
f24.cljs$lang$maxFixedArity = 3;
return f24;

})();
var get_hiccup_children = function (hiccup) {
const content1 = squint_core.rest(hiccup);
if (squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content1)))) {
return squint_core.rest(content1)} else {
return content1};

};
var hiccup_seq_QMARK_ = function (x) {
const and__23153__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23153__auto__1)) {
const and__23153__auto__2 = squint_core.not(squint_core.string_QMARK_(x));
if (and__23153__auto__2) {
return squint_core.not(squint_core.vector_QMARK_(x))} else {
return and__23153__auto__2};
} else {
return and__23153__auto__1};

};
var fully_render_hiccup = function (hiccup, render_state) {
const result1 = (((hiccup == null)) ? (null) : (((squint_core.truth_(hiccup_seq_QMARK_(hiccup))) ? (squint_core.mapv((function (_PERCENT_1) {
return fully_render_hiccup(_PERCENT_1, render_state);

}), hiccup)) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup))) ? ((() => {
const tag2 = squint_core.first(hiccup);
if (squint_core.truth_(squint_core.fn_QMARK_(tag2))) {
return fully_render_hiccup(component__GT_hiccup(normalize_component(hiccup, render_state)), render_state)} else {
const attrs3 = (() => {
const _QMARK_attrs4 = hiccup[1];
if (squint_core.truth_(squint_core.map_QMARK_(_QMARK_attrs4))) {
return _QMARK_attrs4;
};

})();
const children5 = ((squint_core.truth_(attrs3)) ? (squint_core.subvec(hiccup, 2)) : (squint_core.subvec(hiccup, 1)));
const head6 = ((squint_core.truth_(attrs3)) ? ([hiccup[0], attrs3]) : ([hiccup[0]]));
return squint_core.into(head6, squint_core.reduce((function (acc, child) {
const processed7 = fully_render_hiccup(child, render_state);
if ((processed7 == null)) {
return acc} else {
if (squint_core.truth_((() => {
const and__23153__auto__8 = squint_core.vector_QMARK_(processed7);
if (squint_core.truth_(and__23153__auto__8)) {
return ("<>" === processed7[0])} else {
return and__23153__auto__8};

})())) {
return squint_core.into(acc, squint_core.subvec(processed7, 1))} else {
if (squint_core.truth_(hiccup_seq_QMARK_(child))) {
return squint_core.into(acc, processed7)} else {
if ("else") {
return squint_core.conj(acc, processed7)} else {
return null}}}};

}), [], children5));
};

})()) : (((squint_core.truth_(squint_core.map_QMARK_(hiccup))) ? (fully_render_hiccup(squint_core.get(hiccup, "reagent-render")(), render_state)) : ((("else") ? (hiccup) : (null))))))))));
return result1;

};
var unmount_node_and_children = function (node) {
if (squint_core.truth_(node)) {
const temp__22756__auto__1 = node["---ref-fn"];
if (squint_core.truth_(temp__22756__auto__1)) {
const ref_fn2 = temp__22756__auto__1;
ref_fn2(null);
(node["---ref-fn"] = null)};
for (let G__3 of squint_core.iterable(squint_core.vec(node["childNodes"]))) {
const child4 = G__3;
unmount_node_and_children(child4)
}return null;
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
const dom_nodes3 = core_atom(squint_core.vec(dom_a["childNodes"]));
const len_a4 = squint_core.count(children_a1);
const len_b5 = squint_core.count(children_b2);
const parent_ns6 = dom__GT_namespace(dom_a);
let i7 = 0;
while(true){
if ((i7) < (squint_core.min(len_a4, len_b5))) {
const child_a8 = squint_core.nth(children_a1, i7);
const child_b9 = squint_core.nth(children_b2, i7);
const dom_node10 = squint_core.nth(squint_core.deref(dom_nodes3), i7);
const new_dom_node11 = patch(child_a8, child_b9, dom_node10, render_state);
if (squint_core.truth_(squint_core.not_EQ_(dom_node10, new_dom_node11))) {
squint_core.swap_BANG_(dom_nodes3, squint_core.assoc, i7, new_dom_node11)};
let G__12 = (i7 + 1);
i7 = G__12;
continue;
};break;
}
;
if ((len_b5) > (len_a4)) {
for (let G__13 of squint_core.iterable(squint_core.range(len_a4, len_b5))) {
const i14 = G__13;
const temp__22756__auto__15 = hiccup__GT_dom(squint_core.nth(children_b2, i14), parent_ns6, render_state);
if (squint_core.truth_(temp__22756__auto__15)) {
const new_child16 = temp__22756__auto__15;
dom_a.appendChild(new_child16)}
}};
if ((len_a4) > (len_b5)) {
const n__22597__auto__17 = (len_a4) - (len_b5);
let _18 = 0;
while(true){
if ((_18) < (n__22597__auto__17)) {
remove_node_and_unmount_BANG_(dom_a.lastChild);
let G__19 = (_18 + 1);
_18 = G__19;
continue;
};
;break;
}
;
};

};
var get_attrs = function (hiccup) {
const s1 = squint_core.second(hiccup);
if (squint_core.truth_(squint_core.map_QMARK_(s1))) {
return s1} else {
return ({  })};

};
var patch_attributes = function (hiccup_a_rendered, hiccup_b_rendered, dom_a) {
const a_attrs1 = get_attrs(hiccup_a_rendered);
const b_attrs2 = get_attrs(hiccup_b_rendered);
const a_ref3 = squint_core.get(a_attrs1, "ref");
const b_ref4 = squint_core.get(b_attrs2, "ref");
const tag_name5 = dom_a.tagName;
if (squint_core.not(squint_core._EQ_(a_ref3, b_ref4))) {
if (squint_core.truth_(a_ref3)) {
a_ref3(null)};
if (squint_core.truth_(b_ref4)) {
b_ref4(dom_a)};
(dom_a["---ref-fn"] = b_ref4)};
for (let G__6 of squint_core.iterable(a_attrs1)) {
const vec__710 = G__6;
const k11 = squint_core.nth(vec__710, 0, null);
const _12 = squint_core.nth(vec__710, 1, null);
if (squint_core.truth_((() => {
const and__23153__auto__13 = squint_core.not(squint_core.contains_QMARK_(b_attrs2, k11));
if (and__23153__auto__13) {
const and__23153__auto__14 = squint_core.not_EQ_(k11, "ref");
if (squint_core.truth_(and__23153__auto__14)) {
return squint_core.not_EQ_(k11, "xmlns")} else {
return and__23153__auto__14};
} else {
return and__23153__auto__13};

})())) {
if (squint_core.truth_(k11.startsWith("on-"))) {
(dom_a[get_event_name(k11, tag_name5)] = null)} else {
dom_a.removeAttribute(k11)}}
};
for (let G__15 of squint_core.iterable(b_attrs2)) {
const vec__1619 = G__15;
const k20 = squint_core.nth(vec__1619, 0, null);
const v21 = squint_core.nth(vec__1619, 1, null);
if (squint_core.truth_((() => {
const and__23153__auto__22 = squint_core.not_EQ_(k20, "ref");
if (squint_core.truth_(and__23153__auto__22)) {
return squint_core.not_EQ_(k20, "xmlns")} else {
return and__23153__auto__22};

})())) {
const old_v23 = squint_core.get(a_attrs1, k20);
if (squint_core.truth_(k20.startsWith("on-"))) {
(dom_a[get_event_name(k20, tag_name5)] = v21)} else {
if (squint_core.truth_(squint_core.not_EQ_(v21, old_v23))) {
if (squint_core.truth_(("value" === k20))) {
} else {
if (squint_core.truth_(("class" === k20))) {
const class_val24 = ((squint_core.truth_((() => {
const and__23153__auto__25 = squint_core.sequential_QMARK_(v21);
if (squint_core.truth_(and__23153__auto__25)) {
return squint_core.not(squint_core.string_QMARK_(v21))} else {
return and__23153__auto__25};

})())) ? (squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, v21)).join(" ")) : (v21));
if (squint_core.truth_((() => {
const or__23141__auto__26 = (class_val24 == null);
if (or__23141__auto__26) {
return or__23141__auto__26} else {
return ("" === class_val24)};

})())) {
dom_a.removeAttribute("class")} else {
dom_a.setAttribute("class", class_val24)}} else {
if (squint_core.truth_(("style" === k20))) {
const css27 = style_map__GT_css_str(v21);
if (squint_core.truth_(squint_core.seq(css27))) {
dom_a.setAttribute("style", css27)} else {
dom_a.removeAttribute("style")}} else {
if (squint_core.truth_((() => {
const or__23141__auto__28 = ("checked" === k20);
if (squint_core.truth_(or__23141__auto__28)) {
return or__23141__auto__28} else {
return ("selected" === k20)};

})())) {
(dom_a[k20] = v21)} else {
if ("else") {
if ((v21 == null)) {
dom_a.removeAttribute(k20)} else {
const val_str29 = ((squint_core.truth_(("style" === k20))) ? (style_map__GT_css_str(v21)) : (v21));
dom_a.setAttributeNS(null, k20, val_str29)}} else {
}}}}}}}}
}return null;

};
var realize_deep = function (x) {
if (squint_core.truth_((() => {
const and__23153__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23153__auto__1)) {
return x["gen"]} else {
return and__23153__auto__1};

})())) {
return squint_core.mapv(realize_deep, x)} else {
if (squint_core.truth_((() => {
const and__23153__auto__2 = squint_core.sequential_QMARK_(x);
if (squint_core.truth_(and__23153__auto__2)) {
return squint_core.not(squint_core.string_QMARK_(x))} else {
return and__23153__auto__2};

})())) {
return squint_core.into(squint_core.empty(x), squint_core.map(realize_deep, x))} else {
if ("else") {
return x} else {
return null}}};

};
var patch = function (hiccup_a_rendered, hiccup_b_rendered, dom_a, render_state) {
const hiccup_a_realized1 = realize_deep(hiccup_a_rendered);
const hiccup_b_realized2 = realize_deep(hiccup_b_rendered);
if (squint_core.truth_(squint_core._EQ_(hiccup_a_realized1, hiccup_b_realized2))) {
return dom_a} else {
if (squint_core.truth_((() => {
const or__23141__auto__3 = squint_core.not(squint_core.vector_QMARK_(hiccup_a_realized1));
if (or__23141__auto__3) {
return or__23141__auto__3} else {
const or__23141__auto__4 = squint_core.not(squint_core.vector_QMARK_(hiccup_b_realized2));
if (or__23141__auto__4) {
return or__23141__auto__4} else {
return squint_core.not_EQ_(squint_core.first(hiccup_a_realized1), squint_core.first(hiccup_b_realized2))};
};

})())) {
const parent5 = dom_a.parentNode;
const parent_ns6 = dom__GT_namespace(parent5);
const new_node7 = hiccup__GT_dom(hiccup_b_realized2, parent_ns6, render_state);
unmount_node_and_children(dom_a);
dom_a.replaceWith(new_node7);
return new_node7;
} else {
if ("else") {
patch_attributes(hiccup_a_realized1, hiccup_b_rendered, dom_a);
patch_children(hiccup_a_realized1, hiccup_b_rendered, dom_a, render_state);
const a_attrs8 = get_attrs(hiccup_a_realized1);
const b_attrs9 = get_attrs(hiccup_b_rendered);
const b_value10 = squint_core.get(b_attrs9, "value");
if (squint_core.truth_((() => {
const and__23153__auto__11 = squint_core.contains_QMARK_(b_attrs9, "value");
if (squint_core.truth_(and__23153__auto__11)) {
return squint_core.not_EQ_(squint_core.get(a_attrs8, "value"), b_value10)} else {
return and__23153__auto__11};

})())) {
if (squint_core.truth_((() => {
const and__23153__auto__12 = ("SELECT" === dom_a.tagName);
if (squint_core.truth_(and__23153__auto__12)) {
return dom_a.multiple} else {
return and__23153__auto__12};

})())) {
const value_set13 = squint_core.set(b_value10);
for (let G__14 of squint_core.iterable(dom_a.options)) {
const opt15 = G__14;
(opt15["selected"] = squint_core.contains_QMARK_(value_set13, opt15.value))
}} else {
(dom_a["value"] = b_value10)}};
return dom_a;
} else {
return null}}};

};
var modify_dom = function (runtime, normalized_component) {
remove_watchers_for_component(runtime, normalized_component);
const temp__22756__auto__1 = (() => {
const and__23153__auto__2 = runtime;
if (squint_core.truth_(and__23153__auto__2)) {
return runtime_mounted_info(runtime, normalized_component)} else {
return and__23153__auto__2};

})();
if (squint_core.truth_(temp__22756__auto__1)) {
const mounted_info3 = temp__22756__auto__1;
const map__45 = mounted_info3;
const hiccup6 = squint_core.get(map__45, "hiccup");
const dom7 = squint_core.get(map__45, "dom");
const container8 = squint_core.get(map__45, "container");
const base_namespace9 = squint_core.get(map__45, "base-namespace");
const render_state10 = create_render_state(({ "normalized-component": normalized_component, "container": container8, "base-namespace": (() => {
const or__23141__auto__11 = base_namespace9;
if (squint_core.truth_(or__23141__auto__11)) {
return or__23141__auto__11} else {
return dom__GT_namespace(container8)};

})(), "runtime": runtime }));
return (() => {
try{
squint_core.swap_BANG_(render_state10, squint_core.assoc, "positional-key-counter", 0);
const new_hiccup_unrendered12 = with_watcher_bound(normalized_component, render_state10, (function () {
return component__GT_hiccup(normalized_component);

}));
const _13 = squint_core.swap_BANG_(render_state10, squint_core.assoc, "positional-key-counter", 0);
const new_hiccup_rendered14 = fully_render_hiccup(new_hiccup_unrendered12, render_state10);
if (squint_core.truth_((() => {
const and__23153__auto__15 = squint_core.vector_QMARK_(hiccup6);
if (squint_core.truth_(and__23153__auto__15)) {
return ("<>" === squint_core.first(hiccup6))} else {
return and__23153__auto__15};

})())) {
squint_core.swap_BANG_(render_state10, squint_core.assoc, "positional-key-counter", 0);
patch_children(hiccup6, new_hiccup_rendered14, container8, render_state10);
const base_ns16 = squint_core.get(squint_core.deref(render_state10), "base-namespace");
return assoc_runtime_mounted_info_BANG_(runtime, normalized_component, ({ "hiccup": new_hiccup_rendered14, "dom": dom7, "container": container8, "base-namespace": base_ns16, "runtime": runtime }));
} else {
const _17 = squint_core.swap_BANG_(render_state10, squint_core.assoc, "positional-key-counter", 0);
const new_dom18 = patch(hiccup6, new_hiccup_rendered14, dom7, render_state10);
const base_ns19 = squint_core.get(squint_core.deref(render_state10), "base-namespace");
assoc_runtime_mounted_info_BANG_(runtime, normalized_component, ({ "hiccup": new_hiccup_rendered14, "dom": new_dom18, "container": container8, "base-namespace": base_ns19, "runtime": runtime }));
if (squint_core.truth_(squint_core.not_EQ_(dom7, new_dom18))) {
(container8["innerHTML"] = "");
return container8.appendChild(new_dom18);
};
};
}
finally{
squint_core.swap_BANG_(render_state10, squint_core.assoc, "active", false)}

})();
};

};
var notify_watchers = function (watchers) {
for (let G__1 of squint_core.iterable(squint_core.vals((() => {
const or__23141__auto__2 = squint_core.deref(watchers);
if (squint_core.truth_(or__23141__auto__2)) {
return or__23141__auto__2} else {
return empty_js_map()};

})()))) {
const watcher3 = G__1;
if (squint_core.truth_(watcher3)) {
if (squint_core.truth_(should_defer_watcher_QMARK_(watcher3))) {
queue_watcher_BANG_(watcher3)} else {
run_watcher_now(watcher3)}}
}return null;

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
const temp__22756__auto__1 = squint_core.get(squint_core.deref(roots), container);
if (squint_core.truth_(temp__22756__auto__1)) {
const map__23 = temp__22756__auto__1;
const runtime4 = squint_core.get(map__23, "runtime");
if (squint_core.truth_(runtime4)) {
remove_all_runtime_watchers_BANG_(runtime4)};
if (squint_core.truth_(runtime4)) {
squint_core.swap_BANG_(runtime4, (function (state) {
return squint_core.assoc(squint_core.assoc(squint_core.assoc(squint_core.assoc(squint_core.assoc(state, "mounted-components", empty_js_map()), "component-instances", empty_js_map()), "pending-watchers", []), "watcher-flush-scheduled?", false), "subscriptions", empty_js_map());

}))};
squint_core.swap_BANG_(roots, squint_core.dissoc, container)};
for (let G__5 of squint_core.iterable(squint_core.vec(container["childNodes"]))) {
const child6 = G__5;
remove_node_and_unmount_BANG_(child6)
}return null;

};
var do_render = function (normalized_component, container, render_state) {
unmount_components(container);
squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", 0);
return (() => {
try{
const runtime4 = render_state_runtime(render_state);
const base_ns5 = squint_core.get(squint_core.deref(render_state), "base-namespace");
const vec__16 = add_modify_dom_watcher_on_ratom_deref(normalized_component, render_state);
const hiccup7 = squint_core.nth(vec__16, 0, null);
const dom8 = squint_core.nth(vec__16, 1, null);
const _9 = squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", 0);
const hiccup_rendered10 = fully_render_hiccup(hiccup7, render_state);
container.appendChild(dom8);
assoc_runtime_mounted_info_BANG_(runtime4, normalized_component, ({ "hiccup": hiccup_rendered10, "dom": dom8, "container": container, "base-namespace": base_ns5, "runtime": runtime4 }));
if (squint_core.truth_(container)) {
return squint_core.swap_BANG_(roots, squint_core.assoc, container, ({ "container": container, "component": normalized_component, "runtime": runtime4 }));
};
}
finally{
squint_core.swap_BANG_(render_state, squint_core.assoc, "active", false)}

})();

};
var ratom = function (initial_value) {
const a1 = core_atom(initial_value);
const orig_deref2 = a1["_deref"];
const orig_reset_BANG_3 = a1["_reset_BANG_"];
(a1["watchers"] = core_atom(empty_js_map()));
(a1["cursors"] = core_atom(new Set([])));
(a1["_deref"] = (function () {
if (squint_core.truth_(_STAR_watcher_STAR_)) {
const watchers4 = a1["watchers"];
const current5 = squint_core.deref(watchers4);
const watcher_key6 = watcher_entry_key(_STAR_watcher_STAR_);
if (squint_core.truth_(squint_core.contains_QMARK_(current5, watcher_key6))) {
} else {
squint_core.swap_BANG_(watchers4, (function (state) {
return squint_core.assoc((() => {
const or__23141__auto__7 = state;
if (squint_core.truth_(or__23141__auto__7)) {
return or__23141__auto__7} else {
return empty_js_map()};

})(), watcher_key6, _STAR_watcher_STAR_);

}))};
register_watcher_with_host_BANG_(a1, watchers4, _STAR_watcher_STAR_)};
return orig_deref2.call(a1);

}));
(a1["_reset_BANG_"] = (function (new_val) {
const res8 = orig_reset_BANG_3.call(a1, new_val);
notify_watchers(a1["watchers"]);
for (let G__9 of squint_core.iterable(squint_core.deref(a1["cursors"]))) {
const c10 = G__9;
notify_watchers(c10["watchers"])
};
return res8;

}));
return a1;

};
var cursor = function (the_ratom, path) {
const cursors1 = the_ratom["cursors"];
const found_cursor2 = squint_core.some((function (c) {
if (squint_core.truth_(squint_core._EQ_(path, c["path"]))) {
return c;
};

}), squint_core.deref(cursors1));
if ((found_cursor2 == null)) {
const watchers3 = core_atom(empty_js_map());
const this_cursor4 = squint_core.js_obj();
(this_cursor4["_deref"] = (function () {
if (squint_core.truth_(_STAR_watcher_STAR_)) {
const current5 = squint_core.deref(watchers3);
const watcher_key6 = watcher_entry_key(_STAR_watcher_STAR_);
if (squint_core.truth_(squint_core.contains_QMARK_(current5, watcher_key6))) {
} else {
squint_core.swap_BANG_(watchers3, (function (state) {
return squint_core.assoc((() => {
const or__23141__auto__7 = state;
if (squint_core.truth_(or__23141__auto__7)) {
return or__23141__auto__7} else {
return empty_js_map()};

})(), watcher_key6, _STAR_watcher_STAR_);

}))};
register_watcher_with_host_BANG_(this_cursor4, watchers3, _STAR_watcher_STAR_)};
const old_watcher8 = _STAR_watcher_STAR_;
return (() => {
try{
_STAR_watcher_STAR_ = null;
return squint_core.get_in(squint_core.deref(the_ratom), path);
}
finally{
_STAR_watcher_STAR_ = old_watcher8}

})();

}));
(this_cursor4["_swap"] = (() => {
const f27 = (function (var_args) {
const args289 = [];
const len__23200__auto__10 = arguments.length;
let i2911 = 0;
while(true){
if ((i2911) < (len__23200__auto__10)) {
args289.push((arguments[i2911]));
let G__12 = (i2911 + 1);
i2911 = G__12;
continue;
};break;
}
;
const argseq__23384__auto__13 = (((1) < (args289.length)) ? (args289.slice(1)) : (null));
return f27.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23384__auto__13);

});
f27.cljs$core$IFn$_invoke$arity$variadic = (function (f, args) {
return squint_core.swap_BANG_(the_ratom, (function (current_state) {
const current_cursor_value14 = squint_core.get_in(current_state, path);
const new_cursor_value15 = squint_core.apply(f, current_cursor_value14, args);
return squint_core.assoc_in(current_state, path, new_cursor_value15);

}));

});
f27.cljs$lang$maxFixedArity = 1;
return f27;

})());
(this_cursor4["watchers"] = watchers3);
(this_cursor4["path"] = path);
squint_core.swap_BANG_(cursors1, squint_core.conj, this_cursor4);
return this_cursor4;
} else {
return found_cursor2};

};
var reaction = (() => {
const f31 = (function (var_args) {
const args321 = [];
const len__23200__auto__2 = arguments.length;
let i333 = 0;
while(true){
if ((i333) < (len__23200__auto__2)) {
args321.push((arguments[i333]));
let G__4 = (i333 + 1);
i333 = G__4;
continue;
};break;
}
;
const argseq__23384__auto__5 = (((1) < (args321.length)) ? (args321.slice(1)) : (null));
return f31.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23384__auto__5);

});
f31.cljs$core$IFn$_invoke$arity$variadic = (function (f, params) {
const ra6 = ratom(null);
const watcher7 = (function () {
return squint_core.reset_BANG_(ra6, squint_core.apply(f, params));

});
const old_watcher8 = _STAR_watcher_STAR_;
return (() => {
try{
_STAR_watcher_STAR_ = watcher7;
watcher7();
const reaction_obj9 = squint_core.js_obj("_deref", (function () {
return squint_core.deref(ra6);

}), "_swap", (() => {
const f35 = (function (var_args) {
const args3610 = [];
const len__23200__auto__11 = arguments.length;
let i3712 = 0;
while(true){
if ((i3712) < (len__23200__auto__11)) {
args3610.push((arguments[i3712]));
let G__13 = (i3712 + 1);
i3712 = G__13;
continue;
};break;
}
;
const argseq__23384__auto__14 = (((0) < (args3610.length)) ? (args3610.slice(0)) : (null));
return f35.cljs$core$IFn$_invoke$arity$variadic(argseq__23384__auto__14);

});
f35.cljs$core$IFn$_invoke$arity$variadic = (function (_) {
throw new Error("Reactions are readonly");

});
f35.cljs$lang$maxFixedArity = 0;
return f35;

})());
(reaction_obj9["watchers"] = ra6["watchers"]);
return reaction_obj9;
}
finally{
_STAR_watcher_STAR_ = old_watcher8}

})();

});
f31.cljs$lang$maxFixedArity = 1;
return f31;

})();
var render = function (component, container) {
const runtime1 = core_atom(({ "runtime-id": squint_core.str("runtime-", squint_core.random_uuid()), "component-instances": empty_js_map(), "pending-watchers": [], "watcher-flush-scheduled?": false, "mounted-components": empty_js_map(), "subscriptions": empty_js_map() }));
const base_ns2 = dom__GT_namespace(container);
const render_state3 = create_render_state(({ "container": container, "base-namespace": base_ns2, "runtime": runtime1 }));
const normalized4 = normalize_component(component, render_state3);
squint_core.swap_BANG_(render_state3, squint_core.assoc, "normalized-component", normalized4);
return do_render(normalized4, container, render_state3);

};
var atom = ratom;

export { roots, reaction, default_namespace, _STAR_watcher_STAR_, atom, cursor, uri__GT_namespace, render, namespaces, entry_tag__GT_namespace }
