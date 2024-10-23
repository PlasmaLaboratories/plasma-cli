"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[570],{3905:(e,t,r)=>{r.d(t,{Zo:()=>p,kt:()=>f});var n=r(7294);function a(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function o(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function i(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?o(Object(r),!0).forEach((function(t){a(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):o(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function l(e,t){if(null==e)return{};var r,n,a=function(e,t){if(null==e)return{};var r,n,a={},o=Object.keys(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||(a[r]=e[r]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(a[r]=e[r])}return a}var c=n.createContext({}),s=function(e){var t=n.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):i(i({},t),e)),r},p=function(e){var t=s(e.components);return n.createElement(c.Provider,{value:t},e.children)},d="mdxType",m={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},u=n.forwardRef((function(e,t){var r=e.components,a=e.mdxType,o=e.originalType,c=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),d=s(r),u=a,f=d["".concat(c,".").concat(u)]||d[u]||m[u]||o;return r?n.createElement(f,i(i({ref:t},p),{},{components:r})):n.createElement(f,i({ref:t},p))}));function f(e,t){var r=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=r.length,i=new Array(o);i[0]=u;var l={};for(var c in t)hasOwnProperty.call(t,c)&&(l[c]=t[c]);l.originalType=e,l[d]="string"==typeof e?e:a,i[1]=l;for(var s=2;s<o;s++)i[s]=r[s];return n.createElement.apply(null,i)}return n.createElement.apply(null,r)}u.displayName="MDXCreateElement"},7184:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>c,contentTitle:()=>i,default:()=>m,frontMatter:()=>o,metadata:()=>l,toc:()=>s});var n=r(7462),a=(r(7294),r(3905));const o={sidebar_position:2},i="Recover a Wallet Keyfile",l={unversionedId:"how-tos/recover-wallet",id:"how-tos/recover-wallet",title:"Recover a Wallet Keyfile",description:"The command to recover a wallet's main key is:",source:"@site/docs/how-tos/recover-wallet.md",sourceDirName:"how-tos",slug:"/how-tos/recover-wallet",permalink:"/strata-cli/docs/current/how-tos/recover-wallet",draft:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"tutorialSidebar",previous:{title:"Initialize a Wallet",permalink:"/strata-cli/docs/current/how-tos/initialize-wallet"},next:{title:"Get the Current Address",permalink:"/strata-cli/docs/current/how-tos/current-address"}},c={},s=[],p={toc:s},d="wrapper";function m(e){let{components:t,...r}=e;return(0,a.kt)(d,(0,n.Z)({},p,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"recover-a-wallet-keyfile"},"Recover a Wallet Keyfile"),(0,a.kt)("p",null,"The command to recover a wallet's main key is:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"strata-cli wallet recover-keys -w $PASSWORD --passphrase $PASSPHRASE -n $NETWORK -o $KEY_FILE --newwalletdb $WALLET_DB --mnemonic this,is,an,example,of,a,mnemonic,string,that,contains,12,words\n")),(0,a.kt)("p",null,"This will use the the mnemonic specified by the ",(0,a.kt)("inlineCode",{parentName:"p"},"--mnemonic")," option to recover the main key of the wallet. The main key will be stored in the file specified by the ",(0,a.kt)("inlineCode",{parentName:"p"},"-o")," option and protected by the password. The valid values for the ",(0,a.kt)("inlineCode",{parentName:"p"},"-n")," option are ",(0,a.kt)("inlineCode",{parentName:"p"},"mainnet"),", ",(0,a.kt)("inlineCode",{parentName:"p"},"testnet"),", and ",(0,a.kt)("inlineCode",{parentName:"p"},"private"),". A new wallet database will be created and stored in the file specified by the ",(0,a.kt)("inlineCode",{parentName:"p"},"--newwalletdb")," option."),(0,a.kt)("p",null,"Note that the passphrase ",(0,a.kt)("strong",{parentName:"p"},"MUST")," be the same passphrase used to initially generate the mnemonic. The password can be different."))}m.isMDXComponent=!0}}]);