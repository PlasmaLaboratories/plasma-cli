"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[6517],{3905:(t,e,r)=>{r.d(e,{Zo:()=>d,kt:()=>m});var n=r(7294);function o(t,e,r){return e in t?Object.defineProperty(t,e,{value:r,enumerable:!0,configurable:!0,writable:!0}):t[e]=r,t}function a(t,e){var r=Object.keys(t);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(t);e&&(n=n.filter((function(e){return Object.getOwnPropertyDescriptor(t,e).enumerable}))),r.push.apply(r,n)}return r}function i(t){for(var e=1;e<arguments.length;e++){var r=null!=arguments[e]?arguments[e]:{};e%2?a(Object(r),!0).forEach((function(e){o(t,e,r[e])})):Object.getOwnPropertyDescriptors?Object.defineProperties(t,Object.getOwnPropertyDescriptors(r)):a(Object(r)).forEach((function(e){Object.defineProperty(t,e,Object.getOwnPropertyDescriptor(r,e))}))}return t}function c(t,e){if(null==t)return{};var r,n,o=function(t,e){if(null==t)return{};var r,n,o={},a=Object.keys(t);for(n=0;n<a.length;n++)r=a[n],e.indexOf(r)>=0||(o[r]=t[r]);return o}(t,e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(t);for(n=0;n<a.length;n++)r=a[n],e.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(t,r)&&(o[r]=t[r])}return o}var s=n.createContext({}),l=function(t){var e=n.useContext(s),r=e;return t&&(r="function"==typeof t?t(e):i(i({},e),t)),r},d=function(t){var e=l(t.components);return n.createElement(s.Provider,{value:e},t.children)},p="mdxType",u={inlineCode:"code",wrapper:function(t){var e=t.children;return n.createElement(n.Fragment,{},e)}},f=n.forwardRef((function(t,e){var r=t.components,o=t.mdxType,a=t.originalType,s=t.parentName,d=c(t,["components","mdxType","originalType","parentName"]),p=l(r),f=o,m=p["".concat(s,".").concat(f)]||p[f]||u[f]||a;return r?n.createElement(m,i(i({ref:e},d),{},{components:r})):n.createElement(m,i({ref:e},d))}));function m(t,e){var r=arguments,o=e&&e.mdxType;if("string"==typeof t||o){var a=r.length,i=new Array(a);i[0]=f;var c={};for(var s in e)hasOwnProperty.call(e,s)&&(c[s]=e[s]);c.originalType=t,c[p]="string"==typeof t?t:o,i[1]=c;for(var l=2;l<a;l++)i[l]=r[l];return n.createElement.apply(null,i)}return n.createElement.apply(null,r)}f.displayName="MDXCreateElement"},3425:(t,e,r)=>{r.d(e,{ZP:()=>c});var n=r(7462),o=(r(7294),r(3905));const a={toc:[]},i="wrapper";function c(t){let{components:e,...r}=t;return(0,o.kt)(i,(0,n.Z)({},a,r,{components:e,mdxType:"MDXLayout"}),(0,o.kt)("p",null,"To broadcast a simple transaction run the following command:"),(0,o.kt)("pre",null,(0,o.kt)("code",null,"strata-cli tx broadcast -i ",r.txFileProved," -h $HOST --port $PORT")),(0,o.kt)("p",null,"This will broadcast the transaction in the file ",(0,o.kt)("code",null,r.txFileProved)," to the network."))}c.isMDXComponent=!0},8787:(t,e,r)=>{r.r(e),r.d(e,{assets:()=>l,contentTitle:()=>c,default:()=>f,frontMatter:()=>i,metadata:()=>s,toc:()=>d});var n=r(7462),o=(r(7294),r(3905)),a=r(3425);const i={sidebar_position:6},c="Broadcast a Transaction",s={unversionedId:"how-tos/broadcast-tx",id:"how-tos/broadcast-tx",title:"Broadcast a Transaction",description:"Before broadcasting, this command will validate the transaction and the proof. If the validation fails, the command will fail and the transaction will not be broadcasted.",source:"@site/docs/how-tos/broadcast-tx.md",sourceDirName:"how-tos",slug:"/how-tos/broadcast-tx",permalink:"/strata-cli/docs/current/how-tos/broadcast-tx",draft:!1,tags:[],version:"current",sidebarPosition:6,frontMatter:{sidebar_position:6},sidebar:"tutorialSidebar",previous:{title:"Prove a Transaction",permalink:"/strata-cli/docs/current/how-tos/prove-simple-tx"},next:{title:"Query the Node Node",permalink:"/strata-cli/docs/current/how-tos/bifrost-query"}},l={},d=[],p={toc:d},u="wrapper";function f(t){let{components:e,...r}=t;return(0,o.kt)(u,(0,n.Z)({},p,r,{components:e,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"broadcast-a-transaction"},"Broadcast a Transaction"),(0,o.kt)(a.ZP,{txFileProved:"$TX_PROVED_FILE",mdxType:"BroadcastTx"}),(0,o.kt)("p",null,"Before broadcasting, this command will validate the transaction and the proof. If the validation fails, the command will fail and the transaction will not be broadcasted."))}f.isMDXComponent=!0}}]);