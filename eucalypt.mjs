import * as squint_core from 'squint-cljs/core.js';
import * as squint from 'squint-cljs/core.js';
var core_atom = squint.atom;
var log = (() => {
try{
if (squint_core.truth_((() => {
const G__211 = globalThis;
const G__212 = (((G__211 == null)) ? (null) : (G__211["process"]));
const G__213 = (((G__212 == null)) ? (null) : (G__212["env"]));
if ((G__213 == null)) {
return null} else {
return G__213["DEBUG"]};

})())) {
return console.log} else {
if (squint_core.truth_((() => {
const G__224 = globalThis;
const G__225 = (((G__224 == null)) ? (null) : (G__224["localStorage"]));
const G__226 = (((G__225 == null)) ? (null) : (G__225.getItem("debug")));
if ((G__226 == null)) {
return null} else {
return G__226.startsWith("eucalypt:")};

})())) {
return console.log} else {
if ("else") {
return squint_core.identity} else {
return null}}};
}
catch(_7){
return squint_core.identity;
}

})();
log("eucalypt.cljs loading...");
var default_namespace = "html";
var namespaces = ({ "html": ({ "uri": "http://www.w3.org/1999/xhtml" }), "svg": ({ "uri": "http://www.w3.org/2000/svg", "entry-tags": new Set(["svg"]), "boundary-tags": new Set(["foreignObject"]) }), "math": ({ "uri": "http://www.w3.org/1998/Math/MathML", "entry-tags": new Set(["math"]), "boundary-tags": new Set(["annotation-xml"]) }) });
var entry_tag__GT_namespace = squint_core.reduce_kv((function (acc, ns, p__23) {
const map__12 = p__23;
const entry_tags3 = squint_core.get(map__12, "entry-tags");
return squint_core.reduce((function (m, tag) {
return squint_core.assoc(m, tag, ns);

}), acc, (() => {
const or__23119__auto__4 = entry_tags3;
if (squint_core.truth_(or__23119__auto__4)) {
return or__23119__auto__4} else {
return new Set([])};

})());

}), ({  }), namespaces);
var uri__GT_namespace = squint_core.reduce_kv((function (acc, ns, p__24) {
const map__12 = p__24;
const uri3 = squint_core.get(map__12, "uri");
if (squint_core.truth_(uri3)) {
return squint_core.assoc(acc, uri3, ns)} else {
return acc};

}), ({  }), namespaces);
var namespace_uri = function (ns_key) {
const or__23119__auto__1 = squint_core.get_in(namespaces, [ns_key, "uri"]);
if (squint_core.truth_(or__23119__auto__1)) {
return or__23119__auto__1} else {
return squint_core.get_in(namespaces, [default_namespace, "uri"])};

};
var normalize_namespace = function (uri) {
const candidate1 = (() => {
const or__23119__auto__2 = uri;
if (squint_core.truth_(or__23119__auto__2)) {
return or__23119__auto__2} else {
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
const and__23133__auto__4 = squint_core.object_QMARK_(p3);
if (squint_core.truth_(and__23133__auto__4)) {
return p3["watchers"]} else {
return and__23133__auto__4};

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
var life_cycle_methods = ({ "component-will-unmount": rm_watchers });
var mounted_components = core_atom(({  }));
var container__GT_mounted_component = core_atom(({  }));
var component_instances = core_atom(({  }));
var positional_key_counter = core_atom(0);
var all_ratoms = core_atom(({  }));
var with_watcher_bound = function (normalized_component, f) {
const old_watcher1 = _STAR_watcher_STAR_;
return (() => {
try{
_STAR_watcher_STAR_ = with_meta_STAR_((function () {
return modify_dom(normalized_component);

}), ({ "normalized-component": normalized_component }));
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
const temp__22784__auto__8 = ratom7["watchers"];
if (squint_core.truth_(temp__22784__auto__8)) {
const watchers9 = temp__22784__auto__8;
squint_core.swap_BANG_(watchers9, (function (watcher_set) {
return squint_core.set(squint_core.remove((function (_PERCENT_1) {
return squint_core._EQ_(squint_core.get(meta_STAR_(_PERCENT_1), "normalized-component"), component);

}), watcher_set));

}))}
}return null;

};
var style_map__GT_css_str = function (style_map) {
return squint_core.apply(squint_core.str, squint_core.map((function (p__25) {
const vec__14 = p__25;
const k5 = squint_core.nth(vec__14, 0, null);
const v6 = squint_core.nth(vec__14, 1, null);
return squint_core.str(k5, ":", v6, ";");

}), style_map));

};
var event_name_map = ({ "on-double-click": "ondblclick" });
var get_event_name = function (k, tag_name) {
if (squint_core.truth_((() => {
const and__23133__auto__1 = squint_core._EQ_(k, "on-change");
if (squint_core.truth_(and__23133__auto__1)) {
return squint_core.get(new Set(["INPUT", "TEXTAREA"]), tag_name)} else {
return and__23133__auto__1};

})())) {
return "oninput"} else {
return squint_core.get(event_name_map, k, k.replaceAll("-", ""))};

};
var set_attributes_BANG_ = function (element, attrs) {
for (let G__1 of squint_core.iterable(attrs)) {
const vec__25 = G__1;
const k6 = squint_core.nth(vec__25, 0, null);
const v7 = squint_core.nth(vec__25, 1, null);
if (squint_core.truth_(squint_core._EQ_("xmlns", k6))) {
} else {
if (squint_core.truth_(squint_core._EQ_("ref", k6))) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
(element["---ref-fn"] = v7);
v7(element)}} else {
if (squint_core.truth_(k6.startsWith("on-"))) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
const event_name8 = get_event_name(k6, element.tagName);
(element[event_name8] = v7)}} else {
if (squint_core.truth_(squint_core._EQ_("style", k6))) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
const css9 = style_map__GT_css_str(v7);
if (squint_core.truth_(squint_core.seq(css9))) {
element.setAttribute("style", css9)} else {
element.removeAttribute("style")}}} else {
if (squint_core.truth_(squint_core._EQ_("class", k6))) {
const class_val10 = ((squint_core.truth_((() => {
const and__23133__auto__11 = squint_core.sequential_QMARK_(v7);
if (squint_core.truth_(and__23133__auto__11)) {
return squint_core.not(squint_core.string_QMARK_(v7))} else {
return and__23133__auto__11};

})())) ? (squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, v7)).join(" ")) : (v7));
if (squint_core.truth_((() => {
const or__23119__auto__12 = (class_val10 == null);
if (or__23119__auto__12) {
return or__23119__auto__12} else {
return squint_core._EQ_("", class_val10)};

})())) {
element.removeAttribute("class")} else {
element.setAttribute("class", class_val10)}} else {
if (squint_core.truth_((() => {
const or__23119__auto__13 = squint_core._EQ_("checked", k6);
if (squint_core.truth_(or__23119__auto__13)) {
return or__23119__auto__13} else {
return squint_core._EQ_("selected", k6)};

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
const or__23119__auto__17 = squint_core.get(attrs_from_hiccup15, "id");
if (squint_core.truth_(or__23119__auto__17)) {
return or__23119__auto__17} else {
return id13};

})();
const class_from_hiccup18 = squint_core.get(attrs_from_hiccup15, "class");
const all_classes19 = (() => {
const tag_classes20 = (() => {
const or__23119__auto__21 = classes14;
if (squint_core.truth_(or__23119__auto__21)) {
return or__23119__auto__21} else {
return []};

})();
const attr_classes22 = (((class_from_hiccup18 == null)) ? ([]) : (((squint_core.truth_(squint_core.string_QMARK_(class_from_hiccup18))) ? ([class_from_hiccup18]) : (((squint_core.truth_((() => {
const and__23133__auto__23 = squint_core.sequential_QMARK_(class_from_hiccup18);
if (squint_core.truth_(and__23133__auto__23)) {
return squint_core.not(squint_core.string_QMARK_(class_from_hiccup18))} else {
return and__23133__auto__23};

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
var create_element = function (hiccup, current_ns) {
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
const temp__22784__auto__12 = hiccup__GT_dom(child11, new_ns8);
if (squint_core.truth_(temp__22784__auto__12)) {
const child_node13 = temp__22784__auto__12;
element9.appendChild(child_node13)}
};
if (squint_core.truth_(squint_core.some_QMARK_(value6))) {
if (squint_core.truth_((() => {
const and__23133__auto__14 = squint_core._EQ_(element9.tagName, "SELECT");
if (squint_core.truth_(and__23133__auto__14)) {
return element9.multiple} else {
return and__23133__auto__14};

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
const temp__22743__auto__1 = f["_eucalypt_id"];
if (squint_core.truth_(temp__22743__auto__1)) {
const id2 = temp__22743__auto__1;
return id2;
} else {
const new_id3 = squint_core.str("fn_", squint_core.random_uuid());
(f["_eucalypt_id"] = new_id3);
return new_id3;
};

};
var normalize_component = function (component) {
log("normalize-component called with:", component);
log("normalize-component: component metadata:", squint_core.meta(component));
if (squint_core.truth_(squint_core.sequential_QMARK_(component))) {
const first_element1 = squint_core.first(component);
const params2 = squint_core.rest(component);
if (squint_core.truth_(squint_core.fn_QMARK_(first_element1))) {
const a_fn3 = first_element1;
const params_vec4 = squint_core.vec(params2);
const component_meta5 = squint_core.meta(component);
const fn_id6 = get_or_create_fn_id(a_fn3);
const instance_key7 = ((squint_core.truth_(squint_core.contains_QMARK_(component_meta5, "key"))) ? (squint_core.str(fn_id6, "_key_", squint_core.get(component_meta5, "key"))) : ((("else") ? (squint_core.str(fn_id6, "_pos_", squint_core.swap_BANG_(positional_key_counter, squint_core.inc))) : (null))));
const shared_key8 = fn_id6;
const cached_instance9 = squint_core.get(squint_core.deref(component_instances), instance_key7);
const cached_shared10 = squint_core.get(squint_core.deref(component_instances), shared_key8);
if (squint_core.truth_(cached_instance9)) {
log("normalize-component: using cached instance for key:", instance_key7);
return squint_core.into([squint_core.get(cached_instance9, "instance")], params_vec4);
} else {
if (squint_core.truth_(cached_shared10)) {
log("normalize-component: using cached shared for key:", shared_key8);
return squint_core.into([squint_core.get(cached_shared10, "instance")], params_vec4);
} else {
if ("else") {
const _11 = log("normalize-component: cache miss for keys:", instance_key7, "and", shared_key8);
const func_or_hiccup12 = squint_core.apply(a_fn3, params_vec4);
if (squint_core.truth_(squint_core.fn_QMARK_(func_or_hiccup12))) {
const closure13 = func_or_hiccup12;
const instance14 = squint_core.merge(life_cycle_methods, ({ "reagent-render": closure13 }));
const result15 = squint_core.into([instance14], params_vec4);
log("normalize-component: Form-2, caching with key:", instance_key7);
squint_core.swap_BANG_(component_instances, squint_core.assoc, instance_key7, ({ "type": "form-2", "instance": instance14 }));
return result15;
} else {
const instance16 = squint_core.merge(life_cycle_methods, ({ "reagent-render": a_fn3 }));
const result17 = squint_core.into([instance16], params_vec4);
log("normalize-component: Form-1, caching with key:", shared_key8);
squint_core.swap_BANG_(component_instances, squint_core.assoc, shared_key8, ({ "type": "form-1", "instance": instance16 }));
return result17;
};
} else {
return null}}};
} else {
if (squint_core.truth_(squint_core.string_QMARK_(first_element1))) {
return squint_core.into([squint_core.assoc(life_cycle_methods, "reagent-render", (function () {
return component;

}))], params2)} else {
if (squint_core.truth_(squint_core.map_QMARK_(first_element1))) {
const component_as_map18 = first_element1;
const render_fn19 = squint_core.get(component_as_map18, "reagent-render");
const comp_with_lifecycle20 = squint_core.into(({ "reagent-render": render_fn19 }), squint_core.map((function (p__26) {
const vec__2124 = p__26;
const k25 = squint_core.nth(vec__2124, 0, null);
const func26 = squint_core.nth(vec__2124, 1, null);
const func227 = squint_core.get(component_as_map18, k25);
const func_func228 = ((squint_core.truth_(func227)) ? (squint_core.comp(func227, func26)) : (func26));
return [k25, func_func228];

}), life_cycle_methods));
return squint_core.into([comp_with_lifecycle20], params2);
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
log("component->hiccup: calling reagent-render with params:", params9);
const result11 = squint_core.apply(reagent_render10, params9);
log("component->hiccup: reagent-render returned:", (() => {
const G__2712 = result11;
if ((G__2712 == null)) {
return null} else {
return G__2712.toString()};

})());
return result11;

};
var hiccup__GT_dom = (() => {
const f28 = (function (...args29) {
const G__301 = args29.length;
switch (G__301) {case 1:
return f28.cljs$core$IFn$_invoke$arity$1(args29[0]);

break;
case 2:
return f28.cljs$core$IFn$_invoke$arity$2(args29[0], args29[1]);

break;
default:
throw new Error(squint_core.str("Invalid arity: ", args29.length))};

});
f28.cljs$core$IFn$_invoke$arity$1 = (function (hiccup) {
return hiccup__GT_dom(hiccup, namespace_uri(default_namespace));

});
f28.cljs$core$IFn$_invoke$arity$2 = (function (hiccup, current_ns) {
log("hiccup->dom called with:", hiccup);
const result3 = ((squint_core.truth_((() => {
const or__23119__auto__4 = squint_core.string_QMARK_(hiccup);
if (squint_core.truth_(or__23119__auto__4)) {
return or__23119__auto__4} else {
return squint_core.number_QMARK_(hiccup)};

})())) ? (document.createTextNode(squint_core.str(hiccup))) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup))) ? ((() => {
const vec__58 = hiccup;
const seq__69 = squint_core.seq(vec__58);
const first__710 = squint_core.first(seq__69);
const seq__611 = squint_core.next(seq__69);
const tag12 = first__710;
const _content13 = seq__611;
if (squint_core.truth_(squint_core.fn_QMARK_(tag12))) {
return hiccup__GT_dom(component__GT_hiccup(normalize_component(hiccup)), current_ns)} else {
if (squint_core.truth_(squint_core.vector_QMARK_(tag12))) {
const fragment14 = document.createDocumentFragment();
for (let G__15 of squint_core.iterable(hiccup)) {
const item16 = G__15;
const temp__22784__auto__17 = hiccup__GT_dom(item16, current_ns);
if (squint_core.truth_(temp__22784__auto__17)) {
const child_node18 = temp__22784__auto__17;
fragment14.appendChild(child_node18)}
};
return fragment14;
} else {
if (squint_core.truth_(squint_core._EQ_("<>", tag12))) {
const fragment19 = document.createDocumentFragment();
for (let G__20 of squint_core.iterable(squint_core.rest(hiccup))) {
const child21 = G__20;
const temp__22784__auto__22 = hiccup__GT_dom(child21, current_ns);
if (squint_core.truth_(temp__22784__auto__22)) {
const child_node23 = temp__22784__auto__22;
fragment19.appendChild(child_node23)}
};
return fragment19;
} else {
if ("else") {
return create_element(hiccup, current_ns)} else {
return null}}}};

})()) : (((squint_core.truth_(squint_core.seq_QMARK_(hiccup))) ? ((() => {
const fragment24 = document.createDocumentFragment();
for (let G__25 of squint_core.iterable(hiccup)) {
const item26 = G__25;
const item_with_meta27 = ((squint_core.truth_((() => {
const and__23133__auto__28 = squint_core.vector_QMARK_(item26);
if (squint_core.truth_(and__23133__auto__28)) {
return squint_core.meta(item26)} else {
return and__23133__auto__28};

})())) ? (squint_core.with_meta(item26, squint_core.meta(item26))) : (item26));
const temp__22784__auto__29 = hiccup__GT_dom(item_with_meta27, current_ns);
if (squint_core.truth_(temp__22784__auto__29)) {
const child_node30 = temp__22784__auto__29;
fragment24.appendChild(child_node30)}
};
return fragment24;

})()) : (((squint_core.truth_(squint_core.fn_QMARK_(hiccup))) ? (hiccup__GT_dom(hiccup(), current_ns)) : (((squint_core.truth_(squint_core.map_QMARK_(hiccup))) ? (hiccup__GT_dom(squint_core.get(hiccup, "reagent-render")(), current_ns)) : (((squint_core.truth_((() => {
const or__23119__auto__31 = (hiccup == null);
if (or__23119__auto__31) {
return or__23119__auto__31} else {
return squint_core.boolean_QMARK_(hiccup)};

})())) ? (null) : ((("else") ? (document.createTextNode(squint_core.str(hiccup))) : (null))))))))))))));
log("hiccup->dom returning:", (() => {
const G__3132 = result3;
if ((G__3132 == null)) {
return null} else {
return G__3132.toString()};

})());
return result3;

});
f28.cljs$lang$maxFixedArity = 2;
return f28;

})();
var hiccup_eq_QMARK_ = function (hiccup_a, hiccup_b) {
const result1 = squint_core._EQ_(hiccup_a, hiccup_b);
log("hiccup-eq?:", result1);
if (squint_core.not(result1)) {
log("hiccup-eq? details: a:", hiccup_a, "b:", hiccup_b)};
return result1;

};
var is_sequence_of_hiccup_elements_QMARK_ = function (x) {
log("is-sequence-of-hiccup-elements? checking:", x);
const result1 = (() => {
const and__23133__auto__2 = squint_core.sequential_QMARK_(x);
if (squint_core.truth_(and__23133__auto__2)) {
const and__23133__auto__3 = squint_core.not(squint_core.string_QMARK_(x));
if (and__23133__auto__3) {
const and__23133__auto__4 = squint_core.seq(x);
if (squint_core.truth_(and__23133__auto__4)) {
return squint_core.every_QMARK_((function (item) {
const or__23119__auto__5 = (item == null);
if (or__23119__auto__5) {
return or__23119__auto__5} else {
return squint_core.vector_QMARK_(item)};

}), x)} else {
return and__23133__auto__4};
} else {
return and__23133__auto__3};
} else {
return and__23133__auto__2};

})();
log("is-sequence-of-hiccup-elements? result:", result1);
return result1;

};
var get_hiccup_children = function (hiccup) {
const content1 = squint_core.rest(hiccup);
if (squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content1)))) {
return squint_core.rest(content1)} else {
return content1};

};
var fully_render_hiccup = function (hiccup) {
log("fully-render-hiccup called with:", hiccup);
const result1 = (((hiccup == null)) ? (null) : (((squint_core.truth_(squint_core.fn_QMARK_(hiccup))) ? (fully_render_hiccup(hiccup())) : (((squint_core.truth_(is_sequence_of_hiccup_elements_QMARK_(hiccup))) ? (squint_core.mapv(fully_render_hiccup, hiccup)) : (((squint_core.truth_((() => {
const and__23133__auto__2 = squint_core.some_QMARK_(hiccup[Symbol.iterator]);
if (squint_core.truth_(and__23133__auto__2)) {
const and__23133__auto__3 = squint_core.not(squint_core.vector_QMARK_(hiccup));
if (and__23133__auto__3) {
return squint_core.not(squint_core.string_QMARK_(hiccup))} else {
return and__23133__auto__3};
} else {
return and__23133__auto__2};

})())) ? (squint_core.mapv(fully_render_hiccup, hiccup)) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup))) ? ((() => {
const tag4 = squint_core.first(hiccup);
if (squint_core.truth_(squint_core.fn_QMARK_(tag4))) {
return fully_render_hiccup(component__GT_hiccup(normalize_component(hiccup)))} else {
const attrs5 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.second(hiccup)))) ? (squint_core.second(hiccup)) : (null));
const children6 = ((squint_core.truth_(attrs5)) ? (squint_core.drop(2, hiccup)) : (squint_core.rest(hiccup)));
const head7 = ((squint_core.truth_(attrs5)) ? ([squint_core.first(hiccup), attrs5]) : ([squint_core.first(hiccup)]));
return squint_core.into(head7, squint_core.reduce((function (acc, child) {
const processed8 = fully_render_hiccup(child);
log("fully-render-hiccup reduce: processing child, processed=", squint_core.str(processed8));
if (squint_core.truth_((() => {
const and__23133__auto__9 = squint_core.sequential_QMARK_(processed8);
if (squint_core.truth_(and__23133__auto__9)) {
const and__23133__auto__10 = squint_core.not(squint_core.string_QMARK_(processed8));
if (and__23133__auto__10) {
return squint_core.empty_QMARK_(processed8)} else {
return and__23133__auto__10};
} else {
return and__23133__auto__9};

})())) {
return acc} else {
const is_seq11 = is_sequence_of_hiccup_elements_QMARK_(processed8);
log("fully-render-hiccup reduce: is-sequence-of-hiccup-elements?=", is_seq11);
if (squint_core.truth_((() => {
const and__23133__auto__12 = squint_core.vector_QMARK_(processed8);
if (squint_core.truth_(and__23133__auto__12)) {
return squint_core._EQ_("<>", squint_core.first(processed8))} else {
return and__23133__auto__12};

})())) {
log("fully-render-hiccup reduce: unpacking fragment");
return squint_core.into(acc, get_hiccup_children(processed8));
} else {
if (squint_core.truth_(is_seq11)) {
log("fully-render-hiccup reduce: unpacking sequence of hiccup elements");
return squint_core.into(acc, processed8);
} else {
if ("else") {
log("fully-render-hiccup reduce: appending single child");
return squint_core.conj(acc, processed8);
} else {
return null}}};
};

}), [], children6));
};

})()) : (((squint_core.truth_(squint_core.map_QMARK_(hiccup))) ? (fully_render_hiccup(squint_core.get(hiccup, "reagent-render")())) : ((("else") ? (hiccup) : (null))))))))))))));
log("fully-render-hiccup returning:", squint_core.str(result1));
return result1;

};
var unmount_node_and_children = function (node) {
if (squint_core.truth_(node)) {
const temp__22784__auto__1 = node["---ref-fn"];
if (squint_core.truth_(temp__22784__auto__1)) {
const ref_fn2 = temp__22784__auto__1;
log("unmount-node-and-children: calling ref-fn for node", squint_core.str(ref_fn2));
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
var patch_children = function (hiccup_a_rendered, hiccup_b_rendered, dom_a) {
log("--- patch-children start for dom:", (() => {
const G__321 = dom_a;
if ((G__321 == null)) {
return null} else {
return G__321.toString()};

})());
const children_a2 = squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, get_hiccup_children(hiccup_a_rendered)));
const children_b3 = squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, get_hiccup_children(hiccup_b_rendered)));
const dom_nodes4 = core_atom(squint_core.vec(dom_a["childNodes"]));
const len_a5 = squint_core.count(children_a2);
const len_b6 = squint_core.count(children_b3);
const len_dom7 = squint_core.count(squint_core.deref(dom_nodes4));
const parent_ns8 = dom__GT_namespace(dom_a);
log("patch-children: len-a:", len_a5, "len-b:", len_b6, "len-dom:", len_dom7);
log("patch-children: children-a:", squint_core.str(children_a2));
log("patch-children: children-b:", squint_core.str(children_b3));
let i9 = 0;
while(true){
if ((i9) < (squint_core.min(len_a5, len_b6))) {
const child_a10 = squint_core.nth(children_a2, i9);
const child_b11 = squint_core.nth(children_b3, i9);
const dom_node12 = squint_core.nth(squint_core.deref(dom_nodes4), i9);
const new_dom_node13 = patch(child_a10, child_b11, dom_node12);
if (squint_core.truth_(squint_core.not_EQ_(dom_node12, new_dom_node13))) {
squint_core.swap_BANG_(dom_nodes4, squint_core.assoc, i9, new_dom_node13)};
let G__14 = (i9 + 1);
i9 = G__14;
continue;
};break;
}
;
if ((len_b6) > (len_a5)) {
for (let G__15 of squint_core.iterable(squint_core.range(len_a5, len_b6))) {
const i16 = G__15;
const temp__22784__auto__17 = hiccup__GT_dom(squint_core.nth(children_b3, i16), parent_ns8);
if (squint_core.truth_(temp__22784__auto__17)) {
const new_child18 = temp__22784__auto__17;
dom_a.appendChild(new_child18)}
}};
if ((len_a5) > (len_b6)) {
const n__22600__auto__19 = (len_a5) - (len_b6);
let _20 = 0;
while(true){
if ((_20) < (n__22600__auto__19)) {
remove_node_and_unmount_BANG_(dom_a.lastChild);
let G__21 = (_20 + 1);
_20 = G__21;
continue;
};
;break;
}
;
};

};
var get_attrs = function (hiccup) {
log("get-attrs called on hiccup:", hiccup);
const s1 = squint_core.second(hiccup);
if (squint_core.truth_(squint_core.map_QMARK_(s1))) {
return s1} else {
return ({  })};

};
var patch_attributes = function (hiccup_a_rendered, hiccup_b_rendered, dom_a) {
log("patch-attributes called with hiccup-a:", hiccup_a_rendered, "hiccup-b:", hiccup_b_rendered);
const a_attrs1 = get_attrs(hiccup_a_rendered);
const b_attrs2 = get_attrs(hiccup_b_rendered);
const a_ref3 = squint_core.get(a_attrs1, "ref");
const b_ref4 = squint_core.get(b_attrs2, "ref");
const tag_name5 = dom_a.tagName;
log("patch-attributes: a-attrs:", a_attrs1, "b-attrs:", b_attrs2);
log("patch-attributes: a-ref:", a_ref3, "b-ref:", b_ref4);
log("patch-attributes: about to compare refs. are they equal?", squint_core._EQ_(a_ref3, b_ref4), "a-ref:", a_ref3, "b-ref:", b_ref4);
if (squint_core.not(squint_core._EQ_(a_ref3, b_ref4))) {
log("patch-attributes: refs are different, handling lifecycle");
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
const and__23133__auto__13 = squint_core.not(squint_core.contains_QMARK_(b_attrs2, k11));
if (and__23133__auto__13) {
const and__23133__auto__14 = squint_core.not_EQ_(k11, "ref");
if (squint_core.truth_(and__23133__auto__14)) {
return squint_core.not_EQ_(k11, "xmlns")} else {
return and__23133__auto__14};
} else {
return and__23133__auto__13};

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
const and__23133__auto__22 = squint_core.not_EQ_(k20, "ref");
if (squint_core.truth_(and__23133__auto__22)) {
return squint_core.not_EQ_(k20, "xmlns")} else {
return and__23133__auto__22};

})())) {
const old_v23 = squint_core.get(a_attrs1, k20);
if (squint_core.truth_(k20.startsWith("on-"))) {
(dom_a[get_event_name(k20, tag_name5)] = v21)} else {
if (squint_core.truth_(squint_core.not_EQ_(v21, old_v23))) {
log("patch-attributes: updating attribute", k20, "from", old_v23, "to", v21, "on", squint_core.str(dom_a));
if (squint_core.truth_(squint_core._EQ_("value", k20))) {
} else {
if (squint_core.truth_(squint_core._EQ_("class", k20))) {
const class_val24 = ((squint_core.truth_((() => {
const and__23133__auto__25 = squint_core.sequential_QMARK_(v21);
if (squint_core.truth_(and__23133__auto__25)) {
return squint_core.not(squint_core.string_QMARK_(v21))} else {
return and__23133__auto__25};

})())) ? (squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, v21)).join(" ")) : (v21));
if (squint_core.truth_((() => {
const or__23119__auto__26 = (class_val24 == null);
if (or__23119__auto__26) {
return or__23119__auto__26} else {
return squint_core._EQ_("", class_val24)};

})())) {
dom_a.removeAttribute("class")} else {
dom_a.setAttribute("class", class_val24)}} else {
if (squint_core.truth_(squint_core._EQ_("style", k20))) {
const css27 = style_map__GT_css_str(v21);
if (squint_core.truth_(squint_core.seq(css27))) {
dom_a.setAttribute("style", css27)} else {
dom_a.removeAttribute("style")}} else {
if (squint_core.truth_((() => {
const or__23119__auto__28 = squint_core._EQ_("checked", k20);
if (squint_core.truth_(or__23119__auto__28)) {
return or__23119__auto__28} else {
return squint_core._EQ_("selected", k20)};

})())) {
(dom_a[k20] = v21)} else {
if ("else") {
if ((v21 == null)) {
dom_a.removeAttribute(k20)} else {
const val_str29 = ((squint_core.truth_(squint_core._EQ_(k20, "style"))) ? (style_map__GT_css_str(v21)) : (v21));
dom_a.setAttributeNS(null, k20, val_str29)}} else {
}}}}}}}}
}return null;

};
var realize_deep = function (x) {
if (squint_core.truth_((() => {
const and__23133__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23133__auto__1)) {
return x["gen"]} else {
return and__23133__auto__1};

})())) {
return squint_core.mapv(realize_deep, x)} else {
if (squint_core.truth_((() => {
const and__23133__auto__2 = squint_core.sequential_QMARK_(x);
if (squint_core.truth_(and__23133__auto__2)) {
return squint_core.not(squint_core.string_QMARK_(x))} else {
return and__23133__auto__2};

})())) {
return squint_core.into(squint_core.empty(x), squint_core.map(realize_deep, x))} else {
if ("else") {
return x} else {
return null}}};

};
var patch = function (hiccup_a_rendered, hiccup_b_rendered, dom_a) {
log("patch: hiccup-a-rendered", squint_core.str(hiccup_a_rendered), "hiccup-b-rendered", squint_core.str(hiccup_b_rendered), "dom-a", squint_core.str(dom_a));
const hiccup_a_realized1 = realize_deep(hiccup_a_rendered);
const hiccup_b_realized2 = realize_deep(hiccup_b_rendered);
const are_equal3 = hiccup_eq_QMARK_(hiccup_a_realized1, hiccup_b_realized2);
log("patch: are-equal is", are_equal3, "(type", typeof are_equal3, ")");
if (squint_core.truth_(are_equal3)) {
log("patch: hiccup is equal, doing nothing");
return dom_a;
} else {
if (squint_core.truth_((() => {
const or__23119__auto__4 = squint_core.not(squint_core.vector_QMARK_(hiccup_a_realized1));
if (or__23119__auto__4) {
return or__23119__auto__4} else {
const or__23119__auto__5 = squint_core.not(squint_core.vector_QMARK_(hiccup_b_realized2));
if (or__23119__auto__5) {
return or__23119__auto__5} else {
return squint_core.not_EQ_(squint_core.first(hiccup_a_realized1), squint_core.first(hiccup_b_realized2))};
};

})())) {
const parent6 = dom_a.parentNode;
const parent_ns7 = dom__GT_namespace(parent6);
const new_node8 = hiccup__GT_dom(hiccup_b_realized2, parent_ns7);
log("patch: replacing node. dom-a:", squint_core.str(dom_a), "new-node:", squint_core.str(new_node8));
if (squint_core.truth_(dom_a)) {
log("patch: replacing node. dom-a.textContent:", dom_a.textContent, "new-node.textContent:", new_node8.textContent)};
unmount_node_and_children(dom_a);
dom_a.replaceWith(new_node8);
return new_node8;
} else {
if ("else") {
log("patch: hiccup not equal, patching children and attributes");
patch_attributes(hiccup_a_realized1, hiccup_b_realized2, dom_a);
patch_children(hiccup_a_realized1, hiccup_b_realized2, dom_a);
const a_attrs9 = get_attrs(hiccup_a_realized1);
const b_attrs10 = get_attrs(hiccup_b_realized2);
const b_value11 = squint_core.get(b_attrs10, "value");
if (squint_core.truth_((() => {
const and__23133__auto__12 = squint_core.contains_QMARK_(b_attrs10, "value");
if (squint_core.truth_(and__23133__auto__12)) {
return squint_core.not_EQ_(squint_core.get(a_attrs9, "value"), b_value11)} else {
return and__23133__auto__12};

})())) {
log("patch: value changed from", squint_core.get(a_attrs9, "value"), "to", b_value11, "on", squint_core.str(dom_a));
if (squint_core.truth_((() => {
const and__23133__auto__13 = squint_core._EQ_(dom_a.tagName, "SELECT");
if (squint_core.truth_(and__23133__auto__13)) {
return dom_a.multiple} else {
return and__23133__auto__13};

})())) {
const value_set14 = squint_core.set(b_value11);
for (let G__15 of squint_core.iterable(dom_a.options)) {
const opt16 = G__15;
(opt16["selected"] = squint_core.contains_QMARK_(value_set14, opt16.value))
}} else {
(dom_a["value"] = b_value11)}};
return dom_a;
} else {
return null}}};

};
var modify_dom = function (normalized_component) {
log("modify-dom called for component:", normalized_component);
remove_watchers_for_component(normalized_component);
squint_core.reset_BANG_(positional_key_counter, 0);
const mounted_info2 = squint_core.get(squint_core.deref(mounted_components), normalized_component);
const map__13 = mounted_info2;
const hiccup4 = squint_core.get(map__13, "hiccup");
const dom5 = squint_core.get(map__13, "dom");
const container6 = squint_core.get(map__13, "container");
const new_hiccup_unrendered7 = with_watcher_bound(normalized_component, (function () {
return component__GT_hiccup(normalized_component);

}));
const new_hiccup_rendered8 = fully_render_hiccup(new_hiccup_unrendered7);
if (squint_core.truth_((() => {
const and__23133__auto__9 = squint_core.vector_QMARK_(hiccup4);
if (squint_core.truth_(and__23133__auto__9)) {
return squint_core._EQ_("<>", squint_core.first(hiccup4))} else {
return and__23133__auto__9};

})())) {
squint_core.reset_BANG_(positional_key_counter, 0);
patch_children(hiccup4, new_hiccup_rendered8, container6);
return squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": new_hiccup_rendered8, "dom": dom5, "container": container6 }));
} else {
const _10 = squint_core.reset_BANG_(positional_key_counter, 0);
const new_dom11 = patch(hiccup4, new_hiccup_rendered8, dom5);
log("modify-dom: new DOM", squint_core.str(new_dom11));
squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": new_hiccup_rendered8, "dom": new_dom11, "container": container6 }));
if (squint_core.truth_(squint_core.not_EQ_(dom5, new_dom11))) {
log("modify-dom: DOM changed, replacing in container");
(container6["innerHTML"] = "");
return container6.appendChild(new_dom11);
};
};

};
var notify_watchers = function (watchers) {
log("notify-watchers called with", squint_core.count(squint_core.deref(watchers)), "watchers");
for (let G__1 of squint_core.iterable(squint_core.deref(watchers))) {
const watcher2 = G__1;
log("calling watcher");
const old_watcher3 = _STAR_watcher_STAR_;
try{
_STAR_watcher_STAR_ = watcher2;
watcher2()}
finally{
_STAR_watcher_STAR_ = old_watcher3}

}return null;

};
var add_modify_dom_watcher_on_ratom_deref = function (normalized_component, base_namespace) {
return with_watcher_bound(normalized_component, (function () {
const reagent_render1 = squint_core.get(squint_core.first(normalized_component), "reagent-render");
const params2 = squint_core.rest(normalized_component);
const hiccup3 = squint_core.apply(reagent_render1, params2);
const dom4 = hiccup__GT_dom(hiccup3, normalize_namespace(base_namespace));
return [hiccup3, dom4];

}));

};
var unmount_components = function (container) {
const temp__22784__auto__1 = squint_core.get(squint_core.deref(container__GT_mounted_component), container);
if (squint_core.truth_(temp__22784__auto__1)) {
const mounted_component2 = temp__22784__auto__1;
remove_watchers_for_component(mounted_component2);
const vec__37 = mounted_component2;
const seq__48 = squint_core.seq(vec__37);
const first__59 = squint_core.first(seq__48);
const seq__410 = squint_core.next(seq__48);
const map__611 = first__59;
const component_will_unmount12 = squint_core.get(map__611, "component-will-unmount");
const _params13 = seq__410;
component_will_unmount12(mounted_component2);
squint_core.swap_BANG_(container__GT_mounted_component, squint_core.dissoc, container)};
squint_core.reset_BANG_(component_instances, ({  }));
for (let G__14 of squint_core.iterable(squint_core.vec(container["childNodes"]))) {
const child15 = G__14;
remove_node_and_unmount_BANG_(child15)
}return null;

};
var do_render = function (normalized_component, container) {
unmount_components(container);
squint_core.reset_BANG_(positional_key_counter, 0);
const container_ns8 = dom__GT_namespace(container);
const vec__19 = normalized_component;
const seq__210 = squint_core.seq(vec__19);
const first__311 = squint_core.first(seq__210);
const seq__212 = squint_core.next(seq__210);
const map__413 = first__311;
const _reagent_render14 = squint_core.get(map__413, "_reagent-render");
const _params15 = seq__212;
const vec__516 = add_modify_dom_watcher_on_ratom_deref(normalized_component, container_ns8);
const hiccup17 = squint_core.nth(vec__516, 0, null);
const dom18 = squint_core.nth(vec__516, 1, null);
const _19 = squint_core.reset_BANG_(positional_key_counter, 0);
const hiccup_rendered20 = fully_render_hiccup(hiccup17);
container.appendChild(dom18);
squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": hiccup_rendered20, "dom": dom18, "container": container }));
return squint_core.swap_BANG_(container__GT_mounted_component, squint_core.assoc, container, normalized_component);

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
log("ratom deref: watcher found, adding to set.");
squint_core.swap_BANG_(a1["watchers"], squint_core.conj, _STAR_watcher_STAR_)};
return orig_deref2.call(a1);

}));
(a1["_reset_BANG_"] = (function (new_val) {
log("ratom _reset_BANG_ called with", new_val);
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
const f33 = (function (var_args) {
const args346 = [];
const len__23080__auto__7 = arguments.length;
let i358 = 0;
while(true){
if ((i358) < (len__23080__auto__7)) {
args346.push((arguments[i358]));
let G__9 = (i358 + 1);
i358 = G__9;
continue;
};break;
}
;
const argseq__23237__auto__10 = (((1) < (args346.length)) ? (args346.slice(1)) : (null));
return f33.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23237__auto__10);

});
f33.cljs$core$IFn$_invoke$arity$variadic = (function (f, args) {
return squint_core.swap_BANG_(the_ratom, (function (current_state) {
const current_cursor_value11 = squint_core.get_in(current_state, path);
const new_cursor_value12 = squint_core.apply(f, current_cursor_value11, args);
return squint_core.assoc_in(current_state, path, new_cursor_value12);

}));

});
f33.cljs$lang$maxFixedArity = 1;
return f33;

})(), "watchers", watchers3, "path", path);
squint_core.swap_BANG_(cursors1, squint_core.conj, this_cursor4);
return this_cursor4;
} else {
return found_cursor2};

};
var reaction = (() => {
const f37 = (function (var_args) {
const args381 = [];
const len__23080__auto__2 = arguments.length;
let i393 = 0;
while(true){
if ((i393) < (len__23080__auto__2)) {
args381.push((arguments[i393]));
let G__4 = (i393 + 1);
i393 = G__4;
continue;
};break;
}
;
const argseq__23237__auto__5 = (((1) < (args381.length)) ? (args381.slice(1)) : (null));
return f37.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23237__auto__5);

});
f37.cljs$core$IFn$_invoke$arity$variadic = (function (f, params) {
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
const f41 = (function (var_args) {
const args4210 = [];
const len__23080__auto__11 = arguments.length;
let i4312 = 0;
while(true){
if ((i4312) < (len__23080__auto__11)) {
args4210.push((arguments[i4312]));
let G__13 = (i4312 + 1);
i4312 = G__13;
continue;
};break;
}
;
const argseq__23237__auto__14 = (((0) < (args4210.length)) ? (args4210.slice(0)) : (null));
return f41.cljs$core$IFn$_invoke$arity$variadic(argseq__23237__auto__14);

});
f41.cljs$core$IFn$_invoke$arity$variadic = (function (_) {
throw new Error("Reactions are readonly");

});
f41.cljs$lang$maxFixedArity = 0;
return f41;

})());
(reaction_obj9["watchers"] = ra6["watchers"]);
return reaction_obj9;
}
finally{
_STAR_watcher_STAR_ = old_watcher8}

})();

});
f37.cljs$lang$maxFixedArity = 1;
return f37;

})();
var render = function (component, container) {
log("render called with component:", component, "and container:", squint_core.str(container));
const normalized_component1 = normalize_component(component);
log("render: normalized-component is", normalized_component1);
return do_render(normalized_component1, container);

};
var render_component = render;
var clear_component_instances_BANG_ = function () {
return squint_core.reset_BANG_(component_instances, ({  }));

};
var atom = ratom;

export { unmount_components, hiccup_eq_QMARK_, do_render, reaction, default_namespace, _STAR_watcher_STAR_, render_component, log, mounted_components, atom, cursor, patch, positional_key_counter, ratom, component_instances, all_ratoms, uri__GT_namespace, clear_component_instances_BANG_, life_cycle_methods, render, namespaces, normalize_component, modify_dom, entry_tag__GT_namespace, container__GT_mounted_component, hiccup__GT_dom, notify_watchers }
