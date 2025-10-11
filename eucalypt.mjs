import * as squint_core from 'squint-cljs/core.js';
import * as squint from 'squint-cljs/core.js';
var core_atom = squint.atom;
var default_namespace = "html";
var namespaces = ({ "html": ({ "uri": "http://www.w3.org/1999/xhtml" }), "svg": ({ "uri": "http://www.w3.org/2000/svg", "entry-tags": new Set(["svg"]), "boundary-tags": new Set(["foreignObject"]) }), "math": ({ "uri": "http://www.w3.org/1998/Math/MathML", "entry-tags": new Set(["math"]), "boundary-tags": new Set(["annotation-xml"]) }) });
var entry_tag__GT_namespace = squint_core.reduce_kv((function (acc, ns, p__22) {
const map__12 = p__22;
const entry_tags3 = squint_core.get(map__12, "entry-tags");
return squint_core.reduce((function (m, tag) {
return squint_core.assoc(m, tag, ns);

}), acc, (() => {
const or__23145__auto__4 = entry_tags3;
if (squint_core.truth_(or__23145__auto__4)) {
return or__23145__auto__4} else {
return new Set([])};

})());

}), ({  }), namespaces);
var uri__GT_namespace = squint_core.reduce_kv((function (acc, ns, p__23) {
const map__12 = p__23;
const uri3 = squint_core.get(map__12, "uri");
if (squint_core.truth_(uri3)) {
return squint_core.assoc(acc, uri3, ns)} else {
return acc};

}), ({  }), namespaces);
var namespace_uri = function (ns_key) {
const or__23145__auto__1 = squint_core.get_in(namespaces, [ns_key, "uri"]);
if (squint_core.truth_(or__23145__auto__1)) {
return or__23145__auto__1} else {
return squint_core.get_in(namespaces, [default_namespace, "uri"])};

};
var normalize_namespace = function (uri) {
const candidate1 = (() => {
const or__23145__auto__2 = uri;
if (squint_core.truth_(or__23145__auto__2)) {
return or__23145__auto__2} else {
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
var rm_watchers = function (normalized_component) {
const params1 = squint_core.rest(normalized_component);
for (let G__2 of squint_core.iterable(params1)) {
const p3 = G__2;
if (squint_core.truth_((() => {
const and__23156__auto__4 = squint_core.object_QMARK_(p3);
if (squint_core.truth_(and__23156__auto__4)) {
return p3["watchers"]} else {
return and__23156__auto__4};

})())) {
const watchers5 = p3["watchers"];
for (let G__6 of squint_core.iterable(squint_core.deref(watchers5))) {
const w7 = G__6;
if (squint_core.truth_(squint_core._EQ_(squint_core.get(meta_STAR_(w7), "normalized-component"), normalized_component))) {
squint_core.swap_BANG_(watchers5, (function (watchers) {
return squint_core.set(squint_core.remove((function (_PERCENT_1) {
return squint_core._EQ_(w7, _PERCENT_1);

}), watchers));

}))}
}}
}return null;

};
var _STAR_watcher_STAR_ = null;
var mounted_components = core_atom(({  }));
var container__GT_mounted_component = core_atom(({  }));
var component_instances = core_atom(({  }));
var all_ratoms = core_atom(({  }));
var pending_watcher_queue = core_atom([]);
var watcher_flush_scheduled_QMARK_ = core_atom(false);
var create_render_state = function (p__24) {
const map__12 = p__24;
const normalized_component3 = squint_core.get(map__12, "normalized-component");
const container4 = squint_core.get(map__12, "container");
const base_namespace5 = squint_core.get(map__12, "base-namespace");
const state6 = ({ "active": true, "positional-key-counter": 0, "base-namespace": normalize_namespace(base_namespace5) });
const state7 = (() => {
const G__258 = state6;
const G__259 = ((squint_core.truth_(normalized_component3)) ? (squint_core.assoc(G__258, "normalized-component", normalized_component3)) : (G__258));
if (squint_core.truth_(container4)) {
return squint_core.assoc(G__259, "container", container4)} else {
return G__259};

})();
return core_atom(state7);

};
var next_positional_key_BANG_ = function (render_state) {
const next_val1 = (squint_core.get(squint_core.deref(render_state), "positional-key-counter") + 1);
squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", next_val1);
return next_val1;

};
var schedule_watcher_flush_BANG_ = function () {
if (squint_core.truth_(squint_core.deref(watcher_flush_scheduled_QMARK_))) {
return null} else {
squint_core.reset_BANG_(watcher_flush_scheduled_QMARK_, true);
const runner1 = (function () {
return flush_queued_watchers();

});
if (squint_core.truth_(squint_core.some_QMARK_(globalThis.queueMicrotask))) {
return globalThis.queueMicrotask(runner1)} else {
return setTimeout(runner1, 0)};
};

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
var flush_queued_watchers = function () {
const queued1 = squint_core.deref(pending_watcher_queue);
squint_core.reset_BANG_(pending_watcher_queue, []);
squint_core.reset_BANG_(watcher_flush_scheduled_QMARK_, false);
for (let G__2 of squint_core.iterable(queued1)) {
const watcher3 = G__2;
run_watcher_now(watcher3)
}return null;

};
var queue_watcher_BANG_ = function (watcher) {
squint_core.swap_BANG_(pending_watcher_queue, squint_core.conj, watcher);
return schedule_watcher_flush_BANG_();

};
var should_defer_watcher_QMARK_ = function (watcher) {
const meta_info1 = meta_STAR_(watcher);
const defer_fn2 = (() => {
const and__23156__auto__3 = meta_info1;
if (squint_core.truth_(and__23156__auto__3)) {
return squint_core.get(meta_info1, "should-defer?")} else {
return and__23156__auto__3};

})();
return squint_core.boolean$((() => {
const and__23156__auto__4 = squint_core.fn_QMARK_(defer_fn2);
if (squint_core.truth_(and__23156__auto__4)) {
return defer_fn2()} else {
return and__23156__auto__4};

})());

};
var with_watcher_bound = function (normalized_component, render_state, f) {
const old_watcher1 = _STAR_watcher_STAR_;
const watcher_fn2 = with_meta_STAR_((function () {
return modify_dom(normalized_component);

}), ({ "normalized-component": normalized_component, "should-defer?": (function () {
return squint_core.boolean$(squint_core.get(squint_core.deref(render_state), "active"));

}) }));
return (() => {
try{
_STAR_watcher_STAR_ = watcher_fn2;
return f();
}
finally{
_STAR_watcher_STAR_ = old_watcher1}

})();

};
var remove_watchers_for_component = function (component) {
for (let G__1 of squint_core.iterable(squint_core.deref(all_ratoms))) {
const vec__25 = G__1;
const _ratom_id6 = squint_core.nth(vec__25, 0, null);
const ratom7 = squint_core.nth(vec__25, 1, null);
const temp__22795__auto__8 = ratom7["watchers"];
if (squint_core.truth_(temp__22795__auto__8)) {
const watchers9 = temp__22795__auto__8;
squint_core.swap_BANG_(watchers9, (function (watcher_set) {
return squint_core.set(squint_core.remove((function (_PERCENT_1) {
return squint_core._EQ_(squint_core.get(meta_STAR_(_PERCENT_1), "normalized-component"), component);

}), watcher_set));

}))}
}return null;

};
var style_map__GT_css_str = function (style_map) {
return squint_core.apply(squint_core.str, squint_core.map((function (p__26) {
const vec__14 = p__26;
const k5 = squint_core.nth(vec__14, 0, null);
const v6 = squint_core.nth(vec__14, 1, null);
return squint_core.str(k5, ":", v6, ";");

}), style_map));

};
var get_event_name = function (k, tag_name) {
if (squint_core.truth_((() => {
const and__23156__auto__1 = (k === "on-change");
if (squint_core.truth_(and__23156__auto__1)) {
return squint_core.get(new Set(["INPUT", "TEXTAREA"]), tag_name)} else {
return and__23156__auto__1};

})())) {
return "oninput"} else {
if (squint_core.truth_((k === "on-double-click"))) {
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
const and__23156__auto__11 = squint_core.sequential_QMARK_(v7);
if (squint_core.truth_(and__23156__auto__11)) {
return squint_core.not(squint_core.string_QMARK_(v7))} else {
return and__23156__auto__11};

})())) ? (squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, v7)).join(" ")) : (v7));
if (squint_core.truth_((() => {
const or__23145__auto__12 = (class_val10 == null);
if (or__23145__auto__12) {
return or__23145__auto__12} else {
return ("" === class_val10)};

})())) {
element.removeAttribute("class")} else {
element.setAttribute("class", class_val10)}} else {
if (squint_core.truth_((() => {
const or__23145__auto__13 = ("checked" === k6);
if (squint_core.truth_(or__23145__auto__13)) {
return or__23145__auto__13} else {
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
const or__23145__auto__17 = squint_core.get(attrs_from_hiccup15, "id");
if (squint_core.truth_(or__23145__auto__17)) {
return or__23145__auto__17} else {
return id13};

})();
const class_from_hiccup18 = squint_core.get(attrs_from_hiccup15, "class");
const all_classes19 = (() => {
const tag_classes20 = (() => {
const or__23145__auto__21 = classes14;
if (squint_core.truth_(or__23145__auto__21)) {
return or__23145__auto__21} else {
return []};

})();
const attr_classes22 = (((class_from_hiccup18 == null)) ? ([]) : (((squint_core.truth_(squint_core.string_QMARK_(class_from_hiccup18))) ? ([class_from_hiccup18]) : (((squint_core.truth_((() => {
const and__23156__auto__23 = squint_core.sequential_QMARK_(class_from_hiccup18);
if (squint_core.truth_(and__23156__auto__23)) {
return squint_core.not(squint_core.string_QMARK_(class_from_hiccup18))} else {
return and__23156__auto__23};

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
const temp__22795__auto__12 = hiccup__GT_dom(child11, new_ns8, render_state);
if (squint_core.truth_(temp__22795__auto__12)) {
const child_node13 = temp__22795__auto__12;
element9.appendChild(child_node13)}
};
if (squint_core.truth_(squint_core.some_QMARK_(value6))) {
if (squint_core.truth_((() => {
const and__23156__auto__14 = (element9.tagName === "SELECT");
if (squint_core.truth_(and__23156__auto__14)) {
return element9.multiple} else {
return and__23156__auto__14};

})())) {
const value_set15 = squint_core.set(value6);
for (let G__16 of squint_core.iterable(element9.options)) {
const opt17 = G__16;
(opt17["selected"] = squint_core.contains_QMARK_(value_set15, opt17.value))
}} else {
(element9["value"] = value6)}};
return element9;

};
var get_or_create_fn_id = function (f) {
const temp__22727__auto__1 = f["_eucalypt_id"];
if (squint_core.truth_(temp__22727__auto__1)) {
const id2 = temp__22727__auto__1;
return id2;
} else {
const new_id3 = squint_core.str("fn_", squint_core.random_uuid());
(f["_eucalypt_id"] = new_id3);
return new_id3;
};

};
var normalize_component = function (component, render_state) {
if (squint_core.truth_(squint_core.sequential_QMARK_(component))) {
const first_element1 = squint_core.first(component);
const params2 = squint_core.rest(component);
if (squint_core.truth_(squint_core.fn_QMARK_(first_element1))) {
const a_fn3 = first_element1;
const params_vec4 = squint_core.vec(params2);
const component_meta5 = squint_core.meta(component);
const fn_id6 = get_or_create_fn_id(a_fn3);
const instance_key7 = ((squint_core.truth_(squint_core.contains_QMARK_(component_meta5, "key"))) ? (squint_core.str(fn_id6, "_key_", squint_core.get(component_meta5, "key"))) : (squint_core.str(fn_id6, "_pos_", ((squint_core.truth_(render_state)) ? (next_positional_key_BANG_(render_state)) : (squint_core.random_uuid())))));
const shared_key8 = fn_id6;
const cached_instance9 = squint_core.get(squint_core.deref(component_instances), instance_key7);
const cached_shared10 = squint_core.get(squint_core.deref(component_instances), shared_key8);
if (squint_core.truth_(cached_instance9)) {
return squint_core.into([squint_core.get(cached_instance9, "instance")], params_vec4)} else {
if (squint_core.truth_(cached_shared10)) {
return squint_core.into([squint_core.get(cached_shared10, "instance")], params_vec4)} else {
if ("else") {
const func_or_hiccup11 = squint_core.apply(a_fn3, params_vec4);
if (squint_core.truth_(squint_core.fn_QMARK_(func_or_hiccup11))) {
const closure12 = func_or_hiccup11;
const instance13 = ({ "reagent-render": closure12 });
const result14 = squint_core.into([instance13], params_vec4);
squint_core.swap_BANG_(component_instances, squint_core.assoc, instance_key7, ({ "type": "form-2", "instance": instance13 }));
return result14;
} else {
const instance15 = ({ "reagent-render": a_fn3 });
const result16 = squint_core.into([instance15], params_vec4);
squint_core.swap_BANG_(component_instances, squint_core.assoc, shared_key8, ({ "type": "form-1", "instance": instance15 }));
return result16;
};
} else {
return null}}};
} else {
if (squint_core.truth_(squint_core.string_QMARK_(first_element1))) {
return squint_core.into([({ "reagent-render": (function () {
return component;

}) })], params2)} else {
if (squint_core.truth_(squint_core.map_QMARK_(first_element1))) {
const component_as_map17 = first_element1;
const render_fn18 = squint_core.get(component_as_map17, "reagent-render");
const comp_with_lifecycle19 = ({ "reagent-render": render_fn18 });
return squint_core.into([comp_with_lifecycle19], params2);
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
const f27 = (function (...args28) {
const G__291 = args28.length;
switch (G__291) {case 2:
return f27.cljs$core$IFn$_invoke$arity$2(args28[0], args28[1]);

break;
case 3:
return f27.cljs$core$IFn$_invoke$arity$3(args28[0], args28[1], args28[2]);

break;
default:
throw new Error(squint_core.str("Invalid arity: ", args28.length))};

});
f27.cljs$core$IFn$_invoke$arity$2 = (function (hiccup, render_state) {
return hiccup__GT_dom(hiccup, namespace_uri(default_namespace), render_state);

});
f27.cljs$core$IFn$_invoke$arity$3 = (function (hiccup, current_ns, render_state) {
const result3 = ((squint_core.truth_((() => {
const or__23145__auto__4 = squint_core.string_QMARK_(hiccup);
if (squint_core.truth_(or__23145__auto__4)) {
return or__23145__auto__4} else {
return squint_core.number_QMARK_(hiccup)};

})())) ? (document.createTextNode(squint_core.str(hiccup))) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup))) ? ((() => {
const vec__58 = hiccup;
const seq__69 = squint_core.seq(vec__58);
const first__710 = squint_core.first(seq__69);
const seq__611 = squint_core.next(seq__69);
const tag12 = first__710;
const _content13 = seq__611;
if (squint_core.truth_(squint_core.fn_QMARK_(tag12))) {
return hiccup__GT_dom(component__GT_hiccup(normalize_component(hiccup, render_state)), current_ns, render_state)} else {
if (squint_core.truth_(squint_core.vector_QMARK_(tag12))) {
const fragment14 = document.createDocumentFragment();
for (let G__15 of squint_core.iterable(hiccup)) {
const item16 = G__15;
const temp__22795__auto__17 = hiccup__GT_dom(item16, current_ns, render_state);
if (squint_core.truth_(temp__22795__auto__17)) {
const child_node18 = temp__22795__auto__17;
fragment14.appendChild(child_node18)}
};
return fragment14;
} else {
if (squint_core.truth_(("<>" === tag12))) {
const fragment19 = document.createDocumentFragment();
for (let G__20 of squint_core.iterable(squint_core.rest(hiccup))) {
const child21 = G__20;
const temp__22795__auto__22 = hiccup__GT_dom(child21, current_ns, render_state);
if (squint_core.truth_(temp__22795__auto__22)) {
const child_node23 = temp__22795__auto__22;
fragment19.appendChild(child_node23)}
};
return fragment19;
} else {
if ("else") {
return create_element(hiccup, current_ns, render_state)} else {
return null}}}};

})()) : (((squint_core.truth_(squint_core.seq_QMARK_(hiccup))) ? ((() => {
const fragment24 = document.createDocumentFragment();
for (let G__25 of squint_core.iterable(hiccup)) {
const item26 = G__25;
const item_with_meta27 = ((squint_core.truth_((() => {
const and__23156__auto__28 = squint_core.vector_QMARK_(item26);
if (squint_core.truth_(and__23156__auto__28)) {
return squint_core.meta(item26)} else {
return and__23156__auto__28};

})())) ? (squint_core.with_meta(item26, squint_core.meta(item26))) : (item26));
const temp__22795__auto__29 = hiccup__GT_dom(item_with_meta27, current_ns, render_state);
if (squint_core.truth_(temp__22795__auto__29)) {
const child_node30 = temp__22795__auto__29;
fragment24.appendChild(child_node30)}
};
return fragment24;

})()) : (((squint_core.truth_(squint_core.fn_QMARK_(hiccup))) ? (hiccup__GT_dom(hiccup(), current_ns, render_state)) : (((squint_core.truth_(squint_core.map_QMARK_(hiccup))) ? (hiccup__GT_dom(squint_core.get(hiccup, "reagent-render")(), current_ns, render_state)) : (((squint_core.truth_((() => {
const or__23145__auto__31 = (hiccup == null);
if (or__23145__auto__31) {
return or__23145__auto__31} else {
return squint_core.boolean_QMARK_(hiccup)};

})())) ? (null) : ((("else") ? (document.createTextNode(squint_core.str(hiccup))) : (null))))))))))))));
return result3;

});
f27.cljs$lang$maxFixedArity = 3;
return f27;

})();
var get_hiccup_children = function (hiccup) {
const content1 = squint_core.rest(hiccup);
if (squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content1)))) {
return squint_core.rest(content1)} else {
return content1};

};
var hiccup_seq_QMARK_ = function (x) {
const and__23156__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23156__auto__1)) {
const and__23156__auto__2 = squint_core.not(squint_core.string_QMARK_(x));
if (and__23156__auto__2) {
return squint_core.not(squint_core.vector_QMARK_(x))} else {
return and__23156__auto__2};
} else {
return and__23156__auto__1};

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
const and__23156__auto__8 = squint_core.vector_QMARK_(processed7);
if (squint_core.truth_(and__23156__auto__8)) {
return ("<>" === processed7[0])} else {
return and__23156__auto__8};

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
const temp__22795__auto__1 = node["---ref-fn"];
if (squint_core.truth_(temp__22795__auto__1)) {
const ref_fn2 = temp__22795__auto__1;
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
const temp__22795__auto__15 = hiccup__GT_dom(squint_core.nth(children_b2, i14), parent_ns6, render_state);
if (squint_core.truth_(temp__22795__auto__15)) {
const new_child16 = temp__22795__auto__15;
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
const and__23156__auto__13 = squint_core.not(squint_core.contains_QMARK_(b_attrs2, k11));
if (and__23156__auto__13) {
const and__23156__auto__14 = squint_core.not_EQ_(k11, "ref");
if (squint_core.truth_(and__23156__auto__14)) {
return squint_core.not_EQ_(k11, "xmlns")} else {
return and__23156__auto__14};
} else {
return and__23156__auto__13};

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
const and__23156__auto__22 = squint_core.not_EQ_(k20, "ref");
if (squint_core.truth_(and__23156__auto__22)) {
return squint_core.not_EQ_(k20, "xmlns")} else {
return and__23156__auto__22};

})())) {
const old_v23 = squint_core.get(a_attrs1, k20);
if (squint_core.truth_(k20.startsWith("on-"))) {
(dom_a[get_event_name(k20, tag_name5)] = v21)} else {
if (squint_core.truth_(squint_core.not_EQ_(v21, old_v23))) {
if (squint_core.truth_(("value" === k20))) {
} else {
if (squint_core.truth_(("class" === k20))) {
const class_val24 = ((squint_core.truth_((() => {
const and__23156__auto__25 = squint_core.sequential_QMARK_(v21);
if (squint_core.truth_(and__23156__auto__25)) {
return squint_core.not(squint_core.string_QMARK_(v21))} else {
return and__23156__auto__25};

})())) ? (squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, v21)).join(" ")) : (v21));
if (squint_core.truth_((() => {
const or__23145__auto__26 = (class_val24 == null);
if (or__23145__auto__26) {
return or__23145__auto__26} else {
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
const or__23145__auto__28 = ("checked" === k20);
if (squint_core.truth_(or__23145__auto__28)) {
return or__23145__auto__28} else {
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
const and__23156__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23156__auto__1)) {
return x["gen"]} else {
return and__23156__auto__1};

})())) {
return squint_core.mapv(realize_deep, x)} else {
if (squint_core.truth_((() => {
const and__23156__auto__2 = squint_core.sequential_QMARK_(x);
if (squint_core.truth_(and__23156__auto__2)) {
return squint_core.not(squint_core.string_QMARK_(x))} else {
return and__23156__auto__2};

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
const or__23145__auto__3 = squint_core.not(squint_core.vector_QMARK_(hiccup_a_realized1));
if (or__23145__auto__3) {
return or__23145__auto__3} else {
const or__23145__auto__4 = squint_core.not(squint_core.vector_QMARK_(hiccup_b_realized2));
if (or__23145__auto__4) {
return or__23145__auto__4} else {
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
patch_attributes(hiccup_a_realized1, hiccup_b_realized2, dom_a);
patch_children(hiccup_a_realized1, hiccup_b_rendered, dom_a, render_state);
const a_attrs8 = get_attrs(hiccup_a_realized1);
const b_attrs9 = get_attrs(hiccup_b_realized2);
const b_value10 = squint_core.get(b_attrs9, "value");
if (squint_core.truth_((() => {
const and__23156__auto__11 = squint_core.contains_QMARK_(b_attrs9, "value");
if (squint_core.truth_(and__23156__auto__11)) {
return squint_core.not_EQ_(squint_core.get(a_attrs8, "value"), b_value10)} else {
return and__23156__auto__11};

})())) {
if (squint_core.truth_((() => {
const and__23156__auto__12 = ("SELECT" === dom_a.tagName);
if (squint_core.truth_(and__23156__auto__12)) {
return dom_a.multiple} else {
return and__23156__auto__12};

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
var modify_dom = function (normalized_component) {
remove_watchers_for_component(normalized_component);
const temp__22795__auto__1 = squint_core.get(squint_core.deref(mounted_components), normalized_component);
if (squint_core.truth_(temp__22795__auto__1)) {
const mounted_info2 = temp__22795__auto__1;
const map__34 = mounted_info2;
const hiccup5 = squint_core.get(map__34, "hiccup");
const dom6 = squint_core.get(map__34, "dom");
const container7 = squint_core.get(map__34, "container");
const base_namespace8 = squint_core.get(map__34, "base-namespace");
const render_state9 = create_render_state(({ "normalized-component": normalized_component, "container": container7, "base-namespace": (() => {
const or__23145__auto__10 = base_namespace8;
if (squint_core.truth_(or__23145__auto__10)) {
return or__23145__auto__10} else {
return dom__GT_namespace(container7)};

})() }));
return (() => {
try{
squint_core.swap_BANG_(render_state9, squint_core.assoc, "positional-key-counter", 0);
const new_hiccup_unrendered11 = with_watcher_bound(normalized_component, render_state9, (function () {
return component__GT_hiccup(normalized_component);

}));
const _12 = squint_core.swap_BANG_(render_state9, squint_core.assoc, "positional-key-counter", 0);
const new_hiccup_rendered13 = fully_render_hiccup(new_hiccup_unrendered11, render_state9);
if (squint_core.truth_((() => {
const and__23156__auto__14 = squint_core.vector_QMARK_(hiccup5);
if (squint_core.truth_(and__23156__auto__14)) {
return ("<>" === squint_core.first(hiccup5))} else {
return and__23156__auto__14};

})())) {
squint_core.swap_BANG_(render_state9, squint_core.assoc, "positional-key-counter", 0);
patch_children(hiccup5, new_hiccup_rendered13, container7, render_state9);
const base_ns15 = squint_core.get(squint_core.deref(render_state9), "base-namespace");
return squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": new_hiccup_rendered13, "dom": dom6, "container": container7, "base-namespace": base_ns15 }));
} else {
const _16 = squint_core.swap_BANG_(render_state9, squint_core.assoc, "positional-key-counter", 0);
const new_dom17 = patch(hiccup5, new_hiccup_rendered13, dom6, render_state9);
const base_ns18 = squint_core.get(squint_core.deref(render_state9), "base-namespace");
squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": new_hiccup_rendered13, "dom": new_dom17, "container": container7, "base-namespace": base_ns18 }));
if (squint_core.truth_(squint_core.not_EQ_(dom6, new_dom17))) {
(container7["innerHTML"] = "");
return container7.appendChild(new_dom17);
};
};
}
finally{
squint_core.swap_BANG_(render_state9, squint_core.assoc, "active", false)}

})();
};

};
var notify_watchers = function (watchers) {
for (let G__1 of squint_core.iterable(squint_core.deref(watchers))) {
const watcher2 = G__1;
if (squint_core.truth_(watcher2)) {
if (squint_core.truth_(should_defer_watcher_QMARK_(watcher2))) {
queue_watcher_BANG_(watcher2)} else {
run_watcher_now(watcher2)}}
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
const temp__22795__auto__1 = squint_core.get(squint_core.deref(container__GT_mounted_component), container);
if (squint_core.truth_(temp__22795__auto__1)) {
const mounted_component2 = temp__22795__auto__1;
remove_watchers_for_component(mounted_component2);
rm_watchers(mounted_component2);
squint_core.swap_BANG_(container__GT_mounted_component, squint_core.dissoc, container)};
squint_core.reset_BANG_(component_instances, ({  }));
for (let G__3 of squint_core.iterable(squint_core.vec(container["childNodes"]))) {
const child4 = G__3;
remove_node_and_unmount_BANG_(child4)
}return null;

};
var do_render = function (normalized_component, container, render_state) {
unmount_components(container);
squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", 0);
return (() => {
try{
const base_ns4 = squint_core.get(squint_core.deref(render_state), "base-namespace");
const vec__15 = add_modify_dom_watcher_on_ratom_deref(normalized_component, render_state);
const hiccup6 = squint_core.nth(vec__15, 0, null);
const dom7 = squint_core.nth(vec__15, 1, null);
const _8 = squint_core.swap_BANG_(render_state, squint_core.assoc, "positional-key-counter", 0);
const hiccup_rendered9 = fully_render_hiccup(hiccup6, render_state);
container.appendChild(dom7);
squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": hiccup_rendered9, "dom": dom7, "container": container, "base-namespace": base_ns4 }));
return squint_core.swap_BANG_(container__GT_mounted_component, squint_core.assoc, container, normalized_component);
}
finally{
squint_core.swap_BANG_(render_state, squint_core.assoc, "active", false)}

})();

};
var ratom = function (initial_value) {
const a1 = core_atom(initial_value);
const orig_deref2 = a1["_deref"];
const orig_reset_BANG_3 = a1["_reset_BANG_"];
const ratom_id4 = squint_core.str("ratom-", squint_core.random_uuid());
(a1["ratom-id"] = ratom_id4);
(a1["watchers"] = core_atom(new Set([])));
(a1["cursors"] = core_atom(new Set([])));
(a1["_deref"] = (function () {
if (squint_core.truth_(_STAR_watcher_STAR_)) {
squint_core.swap_BANG_(a1["watchers"], squint_core.conj, _STAR_watcher_STAR_)};
return orig_deref2.call(a1);

}));
(a1["_reset_BANG_"] = (function (new_val) {
const res5 = orig_reset_BANG_3.call(a1, new_val);
notify_watchers(a1["watchers"]);
for (let G__6 of squint_core.iterable(squint_core.deref(a1["cursors"]))) {
const c7 = G__6;
notify_watchers(c7["watchers"])
};
return res5;

}));
squint_core.swap_BANG_(all_ratoms, squint_core.assoc, ratom_id4, a1);
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
const watchers3 = core_atom(new Set([]));
const this_cursor4 = squint_core.js_obj("_deref", (function () {
if (squint_core.truth_(_STAR_watcher_STAR_)) {
squint_core.swap_BANG_(watchers3, squint_core.conj, _STAR_watcher_STAR_)};
const old_watcher5 = _STAR_watcher_STAR_;
return (() => {
try{
_STAR_watcher_STAR_ = null;
return squint_core.get_in(squint_core.deref(the_ratom), path);
}
finally{
_STAR_watcher_STAR_ = old_watcher5}

})();

}), "_swap", (() => {
const f30 = (function (var_args) {
const args316 = [];
const len__23077__auto__7 = arguments.length;
let i328 = 0;
while(true){
if ((i328) < (len__23077__auto__7)) {
args316.push((arguments[i328]));
let G__9 = (i328 + 1);
i328 = G__9;
continue;
};break;
}
;
const argseq__23246__auto__10 = (((1) < (args316.length)) ? (args316.slice(1)) : (null));
return f30.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23246__auto__10);

});
f30.cljs$core$IFn$_invoke$arity$variadic = (function (f, args) {
return squint_core.swap_BANG_(the_ratom, (function (current_state) {
const current_cursor_value11 = squint_core.get_in(current_state, path);
const new_cursor_value12 = squint_core.apply(f, current_cursor_value11, args);
return squint_core.assoc_in(current_state, path, new_cursor_value12);

}));

});
f30.cljs$lang$maxFixedArity = 1;
return f30;

})(), "watchers", watchers3, "path", path);
squint_core.swap_BANG_(cursors1, squint_core.conj, this_cursor4);
return this_cursor4;
} else {
return found_cursor2};

};
var reaction = (() => {
const f34 = (function (var_args) {
const args351 = [];
const len__23077__auto__2 = arguments.length;
let i363 = 0;
while(true){
if ((i363) < (len__23077__auto__2)) {
args351.push((arguments[i363]));
let G__4 = (i363 + 1);
i363 = G__4;
continue;
};break;
}
;
const argseq__23246__auto__5 = (((1) < (args351.length)) ? (args351.slice(1)) : (null));
return f34.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23246__auto__5);

});
f34.cljs$core$IFn$_invoke$arity$variadic = (function (f, params) {
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
const f38 = (function (var_args) {
const args3910 = [];
const len__23077__auto__11 = arguments.length;
let i4012 = 0;
while(true){
if ((i4012) < (len__23077__auto__11)) {
args3910.push((arguments[i4012]));
let G__13 = (i4012 + 1);
i4012 = G__13;
continue;
};break;
}
;
const argseq__23246__auto__14 = (((0) < (args3910.length)) ? (args3910.slice(0)) : (null));
return f38.cljs$core$IFn$_invoke$arity$variadic(argseq__23246__auto__14);

});
f38.cljs$core$IFn$_invoke$arity$variadic = (function (_) {
throw new Error("Reactions are readonly");

});
f38.cljs$lang$maxFixedArity = 0;
return f38;

})());
(reaction_obj9["watchers"] = ra6["watchers"]);
return reaction_obj9;
}
finally{
_STAR_watcher_STAR_ = old_watcher8}

})();

});
f34.cljs$lang$maxFixedArity = 1;
return f34;

})();
var render = function (component, container) {
const base_ns1 = dom__GT_namespace(container);
const render_state2 = create_render_state(({ "container": container, "base-namespace": base_ns1 }));
const normalized3 = normalize_component(component, render_state2);
squint_core.swap_BANG_(render_state2, squint_core.assoc, "normalized-component", normalized3);
return do_render(normalized3, container, render_state2);

};
var render_component = render;
var clear_component_instances_BANG_ = function () {
return squint_core.reset_BANG_(component_instances, ({  }));

};
var atom = ratom;

export { unmount_components, do_render, reaction, default_namespace, _STAR_watcher_STAR_, render_component, watcher_flush_scheduled_QMARK_, mounted_components, atom, cursor, patch, ratom, component_instances, all_ratoms, uri__GT_namespace, clear_component_instances_BANG_, render, pending_watcher_queue, namespaces, normalize_component, modify_dom, entry_tag__GT_namespace, container__GT_mounted_component, hiccup__GT_dom, notify_watchers }
