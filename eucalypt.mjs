import * as squint_core from 'squint-cljs/core.js';
import { isEqual } from 'es-toolkit';
var core_atom = squint_core.atom;
var log = (() => {
const f9 = (function (var_args) {
const args101 = [];
const len__23056__auto__2 = arguments.length;
let i113 = 0;
while(true){
if ((i113) < (len__23056__auto__2)) {
args101.push((arguments[i113]));
let G__4 = (i113 + 1);
i113 = G__4;
continue;
};break;
}
;
const argseq__23208__auto__5 = (((0) < (args101.length)) ? (args101.slice(0)) : (null));
return f9.cljs$core$IFn$_invoke$arity$variadic(argseq__23208__auto__5);

});
f9.cljs$core$IFn$_invoke$arity$variadic = (function (args) {
return (() => {
try{
if ((localStorage.getItem("debug")) === ("eucalypt:*")) {
return console.log.apply(console, args);
};
}
catch(_6){
return null;
}

})();

});
f9.cljs$lang$maxFixedArity = 0;
return f9;

})();
log("eucalypt.cljs loading...");
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
const and__23207__auto__4 = squint_core.object_QMARK_(p3);
if (squint_core.truth_(and__23207__auto__4)) {
return p3["watchers"]} else {
return and__23207__auto__4};

})())) {
const watchers5 = p3["watchers"];
for (let G__6 of squint_core.iterable(squint_core.deref(watchers5))) {
const w7 = G__6;
if ((squint_core.get(meta_STAR_(w7), "normalized-component")) === (normalized_component)) {
squint_core.swap_BANG_(watchers5, (function (watchers) {
return squint_core.set(squint_core.remove((function (_PERCENT_1) {
return (w7) === (_PERCENT_1);

}), watchers));

}))}
}}
}return null;

};
var _STAR_watcher_STAR_ = null;
var _STAR_xml_ns_STAR_ = "http://www.w3.org/1999/xhtml";
var life_cycle_methods = ({ "get-initial-state": (function (_this) {
return null;

}), "component-will-receive-props": squint_core.identity, "should-component-update": squint_core.identity, "component-will-update": squint_core.identity, "component-did-update": squint_core.identity, "component-will-unmount": rm_watchers });
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
const temp__22825__auto__8 = ratom7["watchers"];
if (squint_core.truth_(temp__22825__auto__8)) {
const watchers9 = temp__22825__auto__8;
squint_core.swap_BANG_(watchers9, (function (watcher_set) {
return squint_core.set(squint_core.remove((function (_PERCENT_1) {
return (squint_core.get(meta_STAR_(_PERCENT_1), "normalized-component")) === (component);

}), watcher_set));

}))}
}return null;

};
var style_map__GT_css_str = function (style_map) {
return squint_core.apply(squint_core.str, squint_core.map((function (p__13) {
const vec__14 = p__13;
const k5 = squint_core.nth(vec__14, 0, null);
const v6 = squint_core.nth(vec__14, 1, null);
return squint_core.str(k5, ":", v6, ";");

}), style_map));

};
var event_name_map = ({ "on-double-click": "ondblclick" });
var get_event_name = function (k, tag_name) {
if (squint_core.truth_((() => {
const and__23207__auto__1 = (k) === ("on-change");
if (and__23207__auto__1) {
return squint_core.get(new Set(["INPUT", "TEXTAREA"]), tag_name)} else {
return and__23207__auto__1};

})())) {
return "oninput"} else {
return squint_core.get(event_name_map, k, k.replaceAll("-", ""))};

};
var set_attributes_BANG_ = function (element, attrs) {
for (let G__1 of squint_core.iterable(attrs)) {
const vec__25 = G__1;
const k6 = squint_core.nth(vec__25, 0, null);
const v7 = squint_core.nth(vec__25, 1, null);
if (("xmlns") === (k6)) {
} else {
if (("ref") === (k6)) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
(element["---ref-fn"] = v7);
v7(element)}} else {
if (squint_core.truth_(k6.startsWith("on-"))) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
const event_name8 = get_event_name(k6, element.tagName);
(element[event_name8] = v7)}} else {
if (("style") === (k6)) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
(element["style"] = style_map__GT_css_str(v7))}} else {
if (("class") === (k6)) {
if (squint_core.truth_((() => {
const or__23198__auto__9 = (v7 == null);
if (or__23198__auto__9) {
return or__23198__auto__9} else {
return ("") === (v7)};

})())) {
element.removeAttribute("class")} else {
element.setAttribute("class", v7)}} else {
if (squint_core.truth_((() => {
const or__23198__auto__10 = ("checked") === (k6);
if (or__23198__auto__10) {
return or__23198__auto__10} else {
return ("selected") === (k6)};

})())) {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
(element[k6] = v7)}} else {
if ("else") {
if (squint_core.truth_(squint_core.some_QMARK_(v7))) {
element.setAttributeNS(null, k6, v7)}} else {
}}}}}}}
}return null;

};
var create_element = function (hiccup) {
const vec__14 = hiccup;
const seq__25 = squint_core.seq(vec__14);
const first__36 = squint_core.first(seq__25);
const seq__27 = squint_core.next(seq__25);
const tag8 = first__36;
const content9 = seq__27;
const attrs10 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content9)))) ? (squint_core.first(content9)) : (({  })));
const value11 = squint_core.get(attrs10, "value");
const attrs_without_value12 = squint_core.dissoc(attrs10, "value");
const content13 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.first(content9)))) ? (squint_core.rest(content9)) : (content9));
const old_ns14 = _STAR_xml_ns_STAR_;
const new_ns15 = ((("svg") === (tag8)) ? ("http://www.w3.org/2000/svg") : (old_ns14));
const element16 = document.createElementNS(new_ns15, tag8);
return (() => {
try{
_STAR_xml_ns_STAR_ = new_ns15;
set_attributes_BANG_(element16, attrs_without_value12);
for (let G__17 of squint_core.iterable(content13)) {
const child18 = G__17;
const temp__22825__auto__19 = hiccup__GT_dom(child18);
if (squint_core.truth_(temp__22825__auto__19)) {
const child_node20 = temp__22825__auto__19;
element16.appendChild(child_node20)}
};
if (squint_core.truth_(squint_core.some_QMARK_(value11))) {
if (squint_core.truth_((() => {
const and__23207__auto__21 = (element16.tagName) === ("SELECT");
if (and__23207__auto__21) {
return element16.multiple} else {
return and__23207__auto__21};

})())) {
const value_set22 = squint_core.set(value11);
for (let G__23 of squint_core.iterable(element16.options)) {
const opt24 = G__23;
(opt24["selected"] = squint_core.contains_QMARK_(value_set22, opt24.value))
}} else {
(element16["value"] = value11)}};
return element16;
}
finally{
_STAR_xml_ns_STAR_ = old_ns14}

})();

};
var get_or_create_fn_id = function (f) {
const temp__22760__auto__1 = f["_eucalypt_id"];
if (squint_core.truth_(temp__22760__auto__1)) {
const id2 = temp__22760__auto__1;
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
const comp_with_lifecycle20 = squint_core.into(({ "reagent-render": render_fn19 }), squint_core.map((function (p__14) {
const vec__2124 = p__14;
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
log("component->hiccup: reagent-render returned:", result11);
return result11;

};
var hiccup__GT_dom = function (hiccup) {
log("hiccup->dom called with:", hiccup);
const result1 = ((squint_core.truth_((() => {
const or__23198__auto__2 = squint_core.string_QMARK_(hiccup);
if (squint_core.truth_(or__23198__auto__2)) {
return or__23198__auto__2} else {
return squint_core.number_QMARK_(hiccup)};

})())) ? (document.createTextNode(squint_core.str(hiccup))) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup))) ? ((() => {
const vec__36 = hiccup;
const seq__47 = squint_core.seq(vec__36);
const first__58 = squint_core.first(seq__47);
const seq__49 = squint_core.next(seq__47);
const tag10 = first__58;
const _content11 = seq__49;
if (squint_core.truth_(squint_core.fn_QMARK_(tag10))) {
return hiccup__GT_dom(component__GT_hiccup(normalize_component(hiccup)))} else {
if (squint_core.truth_(squint_core.vector_QMARK_(tag10))) {
const fragment12 = document.createDocumentFragment();
for (let G__13 of squint_core.iterable(hiccup)) {
const item14 = G__13;
const temp__22825__auto__15 = hiccup__GT_dom(item14);
if (squint_core.truth_(temp__22825__auto__15)) {
const child_node16 = temp__22825__auto__15;
fragment12.appendChild(child_node16)}
};
return fragment12;
} else {
if (("<>") === (tag10)) {
const fragment17 = document.createDocumentFragment();
for (let G__18 of squint_core.iterable(squint_core.rest(hiccup))) {
const child19 = G__18;
const temp__22825__auto__20 = hiccup__GT_dom(child19);
if (squint_core.truth_(temp__22825__auto__20)) {
const child_node21 = temp__22825__auto__20;
fragment17.appendChild(child_node21)}
};
return fragment17;
} else {
if ("else") {
return create_element(hiccup)} else {
return null}}}};

})()) : (((squint_core.truth_(squint_core.seq_QMARK_(hiccup))) ? ((() => {
const fragment22 = document.createDocumentFragment();
for (let G__23 of squint_core.iterable(hiccup)) {
const item24 = G__23;
const item_with_meta25 = ((squint_core.truth_((() => {
const and__23207__auto__26 = squint_core.vector_QMARK_(item24);
if (squint_core.truth_(and__23207__auto__26)) {
return squint_core.meta(item24)} else {
return and__23207__auto__26};

})())) ? (squint_core.with_meta(item24, squint_core.meta(item24))) : (item24));
const temp__22825__auto__27 = hiccup__GT_dom(item_with_meta25);
if (squint_core.truth_(temp__22825__auto__27)) {
const child_node28 = temp__22825__auto__27;
fragment22.appendChild(child_node28)}
};
return fragment22;

})()) : (((squint_core.truth_(squint_core.fn_QMARK_(hiccup))) ? (hiccup__GT_dom(hiccup())) : (((squint_core.truth_(squint_core.map_QMARK_(hiccup))) ? (hiccup__GT_dom(squint_core.get(hiccup, "reagent-render")())) : (((squint_core.truth_((() => {
const or__23198__auto__29 = (hiccup == null);
if (or__23198__auto__29) {
return or__23198__auto__29} else {
return squint_core.boolean_QMARK_(hiccup)};

})())) ? (null) : ((("else") ? (document.createTextNode(squint_core.str(hiccup))) : (null))))))))))))));
log("hiccup->dom returning:", result1);
return result1;

};
var hiccup_eq_QMARK_ = function (hiccup_a, hiccup_b) {
const result1 = isEqual(hiccup_a, hiccup_b);
log("hiccup-eq?:", result1);
if (squint_core.not(result1)) {
log("hiccup-eq? details: a:", hiccup_a, "b:", hiccup_b)};
return result1;

};
var is_sequence_of_hiccup_elements_QMARK_ = function (x) {
log("is-sequence-of-hiccup-elements? checking:", x);
const result1 = (() => {
const and__23207__auto__2 = squint_core.sequential_QMARK_(x);
if (squint_core.truth_(and__23207__auto__2)) {
const and__23207__auto__3 = squint_core.not(squint_core.string_QMARK_(x));
if (and__23207__auto__3) {
const and__23207__auto__4 = squint_core.seq(x);
if (squint_core.truth_(and__23207__auto__4)) {
return squint_core.every_QMARK_((function (item) {
const or__23198__auto__5 = (item == null);
if (or__23198__auto__5) {
return or__23198__auto__5} else {
return squint_core.vector_QMARK_(item)};

}), x)} else {
return and__23207__auto__4};
} else {
return and__23207__auto__3};
} else {
return and__23207__auto__2};

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
const and__23207__auto__2 = squint_core.some_QMARK_(hiccup[Symbol.iterator]);
if (squint_core.truth_(and__23207__auto__2)) {
const and__23207__auto__3 = squint_core.not(squint_core.vector_QMARK_(hiccup));
if (and__23207__auto__3) {
return squint_core.not(squint_core.string_QMARK_(hiccup))} else {
return and__23207__auto__3};
} else {
return and__23207__auto__2};

})())) ? (squint_core.mapv(fully_render_hiccup, hiccup)) : (((squint_core.truth_(squint_core.vector_QMARK_(hiccup))) ? ((() => {
const tag4 = squint_core.first(hiccup);
if (squint_core.truth_(squint_core.fn_QMARK_(tag4))) {
return fully_render_hiccup(component__GT_hiccup(normalize_component(hiccup)))} else {
const attrs5 = ((squint_core.truth_(squint_core.map_QMARK_(squint_core.second(hiccup)))) ? (squint_core.second(hiccup)) : (null));
const children6 = ((squint_core.truth_(attrs5)) ? (squint_core.drop(2, hiccup)) : (squint_core.rest(hiccup)));
const head7 = ((squint_core.truth_(attrs5)) ? ([squint_core.first(hiccup), attrs5]) : ([squint_core.first(hiccup)]));
return squint_core.into(head7, squint_core.reduce((function (acc, child) {
const processed8 = fully_render_hiccup(child);
log("fully-render-hiccup reduce: processing child, processed=", processed8);
if (squint_core.truth_((() => {
const and__23207__auto__9 = squint_core.sequential_QMARK_(processed8);
if (squint_core.truth_(and__23207__auto__9)) {
const and__23207__auto__10 = squint_core.not(squint_core.string_QMARK_(processed8));
if (and__23207__auto__10) {
return squint_core.empty_QMARK_(processed8)} else {
return and__23207__auto__10};
} else {
return and__23207__auto__9};

})())) {
return acc} else {
const is_seq11 = is_sequence_of_hiccup_elements_QMARK_(processed8);
log("fully-render-hiccup reduce: is-sequence-of-hiccup-elements?=", is_seq11);
if (squint_core.truth_((() => {
const and__23207__auto__12 = squint_core.vector_QMARK_(processed8);
if (squint_core.truth_(and__23207__auto__12)) {
return ("<>") === (squint_core.first(processed8))} else {
return and__23207__auto__12};

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
log("fully-render-hiccup returning:", result1);
return result1;

};
var unmount_node_and_children = function (node) {
if (squint_core.truth_(node)) {
const temp__22825__auto__1 = node["---ref-fn"];
if (squint_core.truth_(temp__22825__auto__1)) {
const ref_fn2 = temp__22825__auto__1;
log("unmount-node-and-children: calling ref-fn for node", node);
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
log("--- patch-children start for dom:", dom_a.toString());
const children_a1 = squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, get_hiccup_children(hiccup_a_rendered)));
const children_b2 = squint_core.vec(squint_core.remove(squint_core.nil_QMARK_, get_hiccup_children(hiccup_b_rendered)));
const dom_nodes3 = core_atom(squint_core.vec(dom_a["childNodes"]));
const len_a4 = squint_core.count(children_a1);
const len_b5 = squint_core.count(children_b2);
const len_dom6 = squint_core.count(squint_core.deref(dom_nodes3));
log("patch-children: len-a:", len_a4, "len-b:", len_b5, "len-dom:", len_dom6);
log("patch-children: children-a:", children_a1);
log("patch-children: children-b:", children_b2);
let i7 = 0;
while(true){
if ((i7) < (squint_core.min(len_a4, len_b5))) {
const child_a8 = squint_core.nth(children_a1, i7);
const child_b9 = squint_core.nth(children_b2, i7);
const dom_node10 = squint_core.nth(squint_core.deref(dom_nodes3), i7);
const new_dom_node11 = patch(child_a8, child_b9, dom_node10);
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
dom_a.appendChild(hiccup__GT_dom(squint_core.nth(children_b2, i14)))
}};
if ((len_a4) > (len_b5)) {
const n__22602__auto__15 = (len_a4) - (len_b5);
let _16 = 0;
while(true){
if ((_16) < (n__22602__auto__15)) {
remove_node_and_unmount_BANG_(dom_a.lastChild);
let G__17 = (_16 + 1);
_16 = G__17;
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
log("patch-attributes: about to compare refs. are they equal?", (a_ref3) === (b_ref4), "a-ref:", a_ref3, "b-ref:", b_ref4);
if (!((a_ref3) === (b_ref4))) {
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
const and__23207__auto__13 = squint_core.not(squint_core.contains_QMARK_(b_attrs2, k11));
if (and__23207__auto__13) {
const and__23207__auto__14 = squint_core.not_EQ_(k11, "ref");
if (squint_core.truth_(and__23207__auto__14)) {
return squint_core.not_EQ_(k11, "xmlns")} else {
return and__23207__auto__14};
} else {
return and__23207__auto__13};

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
const and__23207__auto__22 = squint_core.not_EQ_(k20, "ref");
if (squint_core.truth_(and__23207__auto__22)) {
return squint_core.not_EQ_(k20, "xmlns")} else {
return and__23207__auto__22};

})())) {
const old_v23 = squint_core.get(a_attrs1, k20);
if (squint_core.truth_(k20.startsWith("on-"))) {
(dom_a[get_event_name(k20, tag_name5)] = v21)} else {
if (squint_core.truth_(squint_core.not_EQ_(v21, old_v23))) {
log("patch-attributes: updating attribute", k20, "from", old_v23, "to", v21, "on", dom_a);
if (("value") === (k20)) {
} else {
if (("class") === (k20)) {
if (squint_core.truth_((() => {
const or__23198__auto__24 = (v21 == null);
if (or__23198__auto__24) {
return or__23198__auto__24} else {
return ("") === (v21)};

})())) {
dom_a.removeAttribute("class")} else {
dom_a.setAttribute("class", v21)}} else {
if (squint_core.truth_((() => {
const or__23198__auto__25 = ("checked") === (k20);
if (or__23198__auto__25) {
return or__23198__auto__25} else {
return ("selected") === (k20)};

})())) {
(dom_a[k20] = v21)} else {
if ("else") {
if ((v21 == null)) {
dom_a.removeAttribute(k20)} else {
const val_str26 = (((k20) === ("style")) ? (style_map__GT_css_str(v21)) : (v21));
dom_a.setAttributeNS(null, k20, val_str26)}} else {
}}}}}}}
}return null;

};
var realize_deep = function (x) {
if (squint_core.truth_((() => {
const and__23207__auto__1 = squint_core.seq_QMARK_(x);
if (squint_core.truth_(and__23207__auto__1)) {
return x["gen"]} else {
return and__23207__auto__1};

})())) {
return squint_core.mapv(realize_deep, x)} else {
if (squint_core.truth_((() => {
const and__23207__auto__2 = squint_core.sequential_QMARK_(x);
if (squint_core.truth_(and__23207__auto__2)) {
return squint_core.not(squint_core.string_QMARK_(x))} else {
return and__23207__auto__2};

})())) {
return squint_core.into(squint_core.empty(x), squint_core.map(realize_deep, x))} else {
if ("else") {
return x} else {
return null}}};

};
var patch = function (hiccup_a_rendered, hiccup_b_rendered, dom_a) {
log("patch: hiccup-a-rendered", hiccup_a_rendered, "hiccup-b-rendered", hiccup_b_rendered, "dom-a", dom_a.toString());
const hiccup_a_realized1 = realize_deep(hiccup_a_rendered);
const hiccup_b_realized2 = realize_deep(hiccup_b_rendered);
const are_equal3 = hiccup_eq_QMARK_(hiccup_a_realized1, hiccup_b_realized2);
log("patch: are-equal is", are_equal3, "(type", typeof are_equal3, ")");
if (squint_core.truth_(are_equal3)) {
log("patch: hiccup is equal, doing nothing");
return dom_a;
} else {
if (squint_core.truth_((() => {
const or__23198__auto__4 = squint_core.not(squint_core.vector_QMARK_(hiccup_a_realized1));
if (or__23198__auto__4) {
return or__23198__auto__4} else {
const or__23198__auto__5 = squint_core.not(squint_core.vector_QMARK_(hiccup_b_realized2));
if (or__23198__auto__5) {
return or__23198__auto__5} else {
return squint_core.not_EQ_(squint_core.first(hiccup_a_realized1), squint_core.first(hiccup_b_realized2))};
};

})())) {
const new_node6 = hiccup__GT_dom(hiccup_b_realized2);
log("patch: replacing node. dom-a:", dom_a, "new-node:", new_node6);
if (squint_core.truth_(dom_a)) {
log("patch: replacing node. dom-a.textContent:", dom_a.textContent, "new-node.textContent:", new_node6.textContent)};
unmount_node_and_children(dom_a);
dom_a.replaceWith(new_node6);
return new_node6;
} else {
if ("else") {
log("patch: hiccup not equal, patching children and attributes");
patch_attributes(hiccup_a_realized1, hiccup_b_realized2, dom_a);
patch_children(hiccup_a_realized1, hiccup_b_realized2, dom_a);
const a_attrs7 = get_attrs(hiccup_a_realized1);
const b_attrs8 = get_attrs(hiccup_b_realized2);
const b_value9 = squint_core.get(b_attrs8, "value");
if (squint_core.truth_((() => {
const and__23207__auto__10 = squint_core.contains_QMARK_(b_attrs8, "value");
if (squint_core.truth_(and__23207__auto__10)) {
return squint_core.not_EQ_(squint_core.get(a_attrs7, "value"), b_value9)} else {
return and__23207__auto__10};

})())) {
log("patch: value changed from", squint_core.get(a_attrs7, "value"), "to", b_value9, "on", dom_a);
if (squint_core.truth_((() => {
const and__23207__auto__11 = (dom_a.tagName) === ("SELECT");
if (and__23207__auto__11) {
return dom_a.multiple} else {
return and__23207__auto__11};

})())) {
const value_set12 = squint_core.set(b_value9);
for (let G__13 of squint_core.iterable(dom_a.options)) {
const opt14 = G__13;
(opt14["selected"] = squint_core.contains_QMARK_(value_set12, opt14.value))
}} else {
(dom_a["value"] = b_value9)}};
return dom_a;
} else {
return null}}};

};
var modify_dom = function (normalized_component) {
log("modify-dom called for component:", normalized_component);
remove_watchers_for_component(normalized_component);
squint_core.reset_BANG_(positional_key_counter, 0);
const mounted_info2 = squint_core.get(squint_core.deref(mounted_components), normalized_component);
const _3 = log("modify-dom: mounted-info from cache:", mounted_info2);
const map__14 = mounted_info2;
const hiccup5 = squint_core.get(map__14, "hiccup");
const dom6 = squint_core.get(map__14, "dom");
const container7 = squint_core.get(map__14, "container");
const new_hiccup_unrendered8 = with_watcher_bound(normalized_component, (function () {
return component__GT_hiccup(normalized_component);

}));
const new_hiccup_rendered9 = fully_render_hiccup(new_hiccup_unrendered8);
if (squint_core.truth_((() => {
const and__23207__auto__10 = squint_core.vector_QMARK_(hiccup5);
if (squint_core.truth_(and__23207__auto__10)) {
return ("<>") === (squint_core.first(hiccup5))} else {
return and__23207__auto__10};

})())) {
squint_core.reset_BANG_(positional_key_counter, 0);
patch_children(hiccup5, new_hiccup_rendered9, container7);
return squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": new_hiccup_rendered9, "dom": dom6, "container": container7 }));
} else {
const _11 = squint_core.reset_BANG_(positional_key_counter, 0);
const new_dom12 = patch(hiccup5, new_hiccup_rendered9, dom6);
log("modify-dom: new DOM", new_dom12);
squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": new_hiccup_rendered9, "dom": new_dom12, "container": container7 }));
if (squint_core.truth_(squint_core.not_EQ_(dom6, new_dom12))) {
log("modify-dom: DOM changed, replacing in container");
(container7["innerHTML"] = "");
return container7.appendChild(new_dom12);
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
var add_modify_dom_watcher_on_ratom_deref = function (normalized_component) {
return with_watcher_bound(normalized_component, (function () {
const reagent_render1 = squint_core.get(squint_core.first(normalized_component), "reagent-render");
const params2 = squint_core.rest(normalized_component);
const hiccup3 = squint_core.apply(reagent_render1, params2);
const dom4 = hiccup__GT_dom(hiccup3);
return [hiccup3, dom4];

}));

};
var unmount_components = function (container) {
const temp__22825__auto__1 = squint_core.get(squint_core.deref(container__GT_mounted_component), container);
if (squint_core.truth_(temp__22825__auto__1)) {
const mounted_component2 = temp__22825__auto__1;
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
const vec__18 = normalized_component;
const seq__29 = squint_core.seq(vec__18);
const first__310 = squint_core.first(seq__29);
const seq__211 = squint_core.next(seq__29);
const map__412 = first__310;
const _reagent_render13 = squint_core.get(map__412, "_reagent-render");
const _params14 = seq__211;
const vec__515 = add_modify_dom_watcher_on_ratom_deref(normalized_component);
const hiccup16 = squint_core.nth(vec__515, 0, null);
const dom17 = squint_core.nth(vec__515, 1, null);
const _18 = squint_core.reset_BANG_(positional_key_counter, 0);
const hiccup_rendered19 = fully_render_hiccup(hiccup16);
container.appendChild(dom17);
squint_core.swap_BANG_(mounted_components, squint_core.assoc, normalized_component, ({ "hiccup": hiccup_rendered19, "dom": dom17, "container": container }));
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
if ((path) === (c["path"])) {
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
const f15 = (function (var_args) {
const args166 = [];
const len__23056__auto__7 = arguments.length;
let i178 = 0;
while(true){
if ((i178) < (len__23056__auto__7)) {
args166.push((arguments[i178]));
let G__9 = (i178 + 1);
i178 = G__9;
continue;
};break;
}
;
const argseq__23208__auto__10 = (((1) < (args166.length)) ? (args166.slice(1)) : (null));
return f15.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23208__auto__10);

});
f15.cljs$core$IFn$_invoke$arity$variadic = (function (f, args) {
return squint_core.swap_BANG_(the_ratom, (function (current_state) {
const current_cursor_value11 = squint_core.get_in(current_state, path);
const new_cursor_value12 = squint_core.apply(f, current_cursor_value11, args);
return squint_core.assoc_in(current_state, path, new_cursor_value12);

}));

});
f15.cljs$lang$maxFixedArity = 1;
return f15;

})(), "watchers", watchers3, "path", path);
squint_core.swap_BANG_(cursors1, squint_core.conj, this_cursor4);
return this_cursor4;
} else {
return found_cursor2};

};
var reaction = (() => {
const f19 = (function (var_args) {
const args201 = [];
const len__23056__auto__2 = arguments.length;
let i213 = 0;
while(true){
if ((i213) < (len__23056__auto__2)) {
args201.push((arguments[i213]));
let G__4 = (i213 + 1);
i213 = G__4;
continue;
};break;
}
;
const argseq__23208__auto__5 = (((1) < (args201.length)) ? (args201.slice(1)) : (null));
return f19.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__23208__auto__5);

});
f19.cljs$core$IFn$_invoke$arity$variadic = (function (f, params) {
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
const f23 = (function (var_args) {
const args2410 = [];
const len__23056__auto__11 = arguments.length;
let i2512 = 0;
while(true){
if ((i2512) < (len__23056__auto__11)) {
args2410.push((arguments[i2512]));
let G__13 = (i2512 + 1);
i2512 = G__13;
continue;
};break;
}
;
const argseq__23208__auto__14 = (((0) < (args2410.length)) ? (args2410.slice(0)) : (null));
return f23.cljs$core$IFn$_invoke$arity$variadic(argseq__23208__auto__14);

});
f23.cljs$core$IFn$_invoke$arity$variadic = (function (_) {
throw new Error("Reactions are readonly");

});
f23.cljs$lang$maxFixedArity = 0;
return f23;

})());
(reaction_obj9["watchers"] = ra6["watchers"]);
return reaction_obj9;
}
finally{
_STAR_watcher_STAR_ = old_watcher8}

})();

});
f19.cljs$lang$maxFixedArity = 1;
return f19;

})();
var render = function (component, container) {
log("render called with component:", component, "and container:", container);
const normalized_component1 = normalize_component(component);
log("render: normalized-component is", normalized_component1);
return do_render(normalized_component1, container);

};
var render_component = render;
var clear_component_instances_BANG_ = function () {
return squint_core.reset_BANG_(component_instances, ({  }));

};
var atom = ratom;

export { unmount_components, hiccup_eq_QMARK_, do_render, reaction, _STAR_watcher_STAR_, render_component, mounted_components, _STAR_xml_ns_STAR_, atom, cursor, patch, positional_key_counter, ratom, component_instances, all_ratoms, clear_component_instances_BANG_, life_cycle_methods, render, normalize_component, modify_dom, container__GT_mounted_component, hiccup__GT_dom, notify_watchers }
