"use strict";

let arrayToFilter = [
    {
        "start": 0.144,
        "end": 0.5003389830508475,
        "text": "what's",
        "diff_kind": "SUB",
        "uuid": "32058c39-2594-4291-9608-e5980e1253fb"
    },
    {
        "start": 0.5003389830508475,
        "end": 0.6191186440677966,
        "text": "up",
        "diff_kind": "SUB",
        "uuid": "b8093582-5171-4734-8e3f-03bff1918cd0"
    },
    {
        "start": 0.6191186440677966,
        "end": 0.856677966101695,
        "text": "guys",
        "diff_kind": "SUB",
        "uuid": "0af07958-0b09-4f0c-969d-2f11de198e68"
    },
    {
        "text": "*",
        "start": 0.768,
        "end": 0.864,
        "type": "WORD",
        "diff_kind": "INS_DEL",
        "uuid": "4919ca06-571d-47cb-b627-107262375921"
    },
    {
        "start": 0.856677966101695,
        "end": 0.9754576271186441,
        "text": "so",
        "diff_kind": "NO_DIFF",
        "uuid": "77d73a7a-ab4c-495b-86d6-c250b804ba35"
    },
    {
        "start": 0.9754576271186441,
        "end": 1.0348474576271187,
        "text": "i",
        "diff_kind": "NO_DIFF",
        "uuid": "a579c1fa-fcfc-4f20-ba58-60aa0d316423"
    },
    {
        "start": 1.0348474576271187,
        "end": 1.272406779661017,
        "text": "want",
        "diff_kind": "NO_DIFF",
        "uuid": "35f60e42-da20-4b23-a492-eb96a8ae4b26"
    },
    {
        "start": 1.272406779661017,
        "end": 1.3911864406779662,
        "text": "to",
        "diff_kind": "NO_DIFF",
        "uuid": "0644e97c-7a30-4e69-b15e-cb918ea04707"
    },
    {
        "start": 1.3911864406779662,
        "end": 1.6287457627118644,
        "text": "show",
        "diff_kind": "NO_DIFF",
        "uuid": "44103818-9afb-4088-9344-352016638ca5"
    },
    {
        "start": 1.6287457627118644,
        "end": 1.8069152542372882,
        "text": "you",
        "diff_kind": "NO_DIFF",
        "uuid": "4da0ef4d-0b4e-45b5-b4c5-ba3583858e73"
    },
    {
        "start": 1.8069152542372882,
        "end": 1.8663050847457627,
        "text": "a",
        "diff_kind": "NO_DIFF",
        "uuid": "322efdc2-c87f-49aa-a85d-7b00241a7ee5"
    },
    {
        "start": 1.8663050847457627,
        "end": 2.2226440677966104,
        "text": "really",
        "diff_kind": "NO_DIFF",
        "uuid": "7917cae2-ae7b-47eb-90d8-07c30d3fcf23"
    },
    {
        "start": 2.2226440677966104,
        "end": 2.4602033898305087,
        "text": "cool",
        "diff_kind": "NO_DIFF",
        "uuid": "e1bbb092-511a-470a-90c5-bc1e5d395a95"
    },
    {
        "start": 2.4602033898305087,
        "end": 2.875932203389831,
        "text": "feature",
        "diff_kind": "NO_DIFF",
        "uuid": "092e3f03-ea99-4eab-9116-f5f30735b854"
    },
    {
        "start": 2.875932203389831,
        "end": 3.113491525423729,
        "text": "that",
        "diff_kind": "NO_DIFF",
        "uuid": "786270f0-0b76-496d-a7d3-e671b21c2d0d"
    },
    {
        "start": 3.113491525423729,
        "end": 3.410440677966102,
        "text": "we've",
        "diff_kind": "NO_DIFF",
        "uuid": "f4293493-19dd-4538-9e8e-831a21c3aa1d"
    },
    {
        "start": 3.410440677966102,
        "end": 3.648,
        "text": "just",
        "diff_kind": "NO_DIFF",
        "uuid": "b19d5a6c-7d34-49c2-8319-1dee57de9a34"
    },
    {
        "start": 3.648,
        "end": 4.128,
        "text": "launched",
        "diff_kind": "NO_DIFF",
        "uuid": "376c8217-733b-4cea-add6-4e3d488ce32d"
    },
    {
        "start": 4.128,
        "end": 4.248,
        "text": "on",
        "diff_kind": "NO_DIFF",
        "uuid": "23b1a5d9-2ed9-4e33-8f36-9178f4f4a059"
    },
    {
        "start": 4.248,
        "end": 4.428,
        "text": "the",
        "diff_kind": "NO_DIFF",
        "uuid": "70814f8d-5a20-4496-ac45-e6354ee775fd"
    },
    {
        "start": 4.428,
        "end": 4.788,
        "text": "source",
        "diff_kind": "NO_DIFF",
        "uuid": "de5df1db-ed6b-46e8-a8e7-8fcb6d339464"
    },
    {
        "start": 4.788,
        "end": 5.268000000000001,
        "text": "platform",
        "diff_kind": "NO_DIFF",
        "uuid": "12eca3ba-1bc3-491e-bba2-7b5c524687c7"
    },
    {
        "start": 5.268000000000001,
        "end": 5.628,
        "text": "called",
        "diff_kind": "SUB",
        "uuid": "d57a3b2d-3df4-4b4f-888c-a8a44fb9c0d9"
    },
    {
        "start": 5.628,
        "end": 5.928000000000001,
        "text": "share",
        "diff_kind": "NO_DIFF",
        "uuid": "b57227d4-90f0-4c36-bff2-0773dc111c21"
    },
    {
        "start": 5.928000000000001,
        "end": 6.048,
        "text": "so",
        "diff_kind": "NO_DIFF",
        "uuid": "d7600958-be89-4611-be78-e142b707274e"
    },
    {
        "start": 6.048,
        "end": 6.288,
        "text": "this",
        "diff_kind": "NO_DIFF",
        "uuid": "c208dae5-35c3-4a61-964f-e7e461d77e0a"
    },
    {
        "start": 6.288,
        "end": 6.468,
        "text": "new",
        "diff_kind": "NO_DIFF",
        "uuid": "b7a13353-d91a-4e98-aac0-fb940c1ad3e2"
    },
    {
        "start": 6.468,
        "end": 7.248,
        "text": "functionality",
        "diff_kind": "NO_DIFF",
        "uuid": "90dcc057-4f36-45de-87d6-c1eead1e59e0"
    },
    {
        "start": 7.248,
        "end": 7.708444444444445,
        "text": "enables",
        "diff_kind": "NO_DIFF",
        "uuid": "41dc395d-7c8f-4505-8fed-9bdd95485b11"
    },
    {
        "start": 7.708444444444445,
        "end": 7.905777777777778,
        "text": "you",
        "diff_kind": "NO_DIFF",
        "uuid": "749d0bdb-81bc-419b-86d6-16f5f51d15cc"
    },
    {
        "start": 7.905777777777778,
        "end": 8.037333333333333,
        "text": "to",
        "diff_kind": "NO_DIFF",
        "uuid": "542dfcfd-007d-4b55-8803-ee31830bf08b"
    },
    {
        "start": 8.037333333333333,
        "end": 8.366222222222223,
        "text": "share",
        "diff_kind": "NO_DIFF",
        "uuid": "52629b7e-794a-43b4-bb5d-1d291a636d48"
    },
    {
        "start": 8.366222222222223,
        "end": 8.76088888888889,
        "text": "videos",
        "diff_kind": "NO_DIFF",
        "uuid": "8d68eeac-1bb6-435f-abdc-b89bc7d8f13a"
    },
    {
        "start": 8.76088888888889,
        "end": 9.28711111111111,
        "text": "straight",
        "diff_kind": "NO_DIFF",
        "uuid": "3a502f20-0271-4de1-83f5-d41026a7dedd"
    },
    {
        "start": 9.28711111111111,
        "end": 9.550222222222223,
        "text": "from",
        "diff_kind": "NO_DIFF",
        "uuid": "280076de-1097-46c5-9a22-3b4f1c829e43"
    },
    {
        "start": 9.550222222222223,
        "end": 9.94488888888889,
        "text": "source",
        "diff_kind": "NO_DIFF",
        "uuid": "239dfcd4-9f6b-450b-85b1-383cd9258554"
    },
    {
        "start": 9.94488888888889,
        "end": 10.076444444444444,
        "text": "to",
        "diff_kind": "NO_DIFF",
        "uuid": "71be9a2d-35cd-4774-b1ca-a681fe640aa1"
    },
    {
        "start": 10.076444444444444,
        "end": 10.339555555555556,
        "text": "your",
        "diff_kind": "NO_DIFF",
        "uuid": "fca86902-6334-49d5-99a7-1b88f0c8008e"
    },
    {
        "start": 10.339555555555556,
        "end": 10.8,
        "text": "clients",
        "diff_kind": "NO_DIFF",
        "uuid": "62bfb024-ca1d-4942-8a4f-7c6030f6e9c6"
    },
    {
        "start": 10.848,
        "end": 10.964459016393443,
        "text": "or",
        "diff_kind": "NO_DIFF",
        "uuid": "3f357ee1-52d9-4ef9-9c45-9deacec74bb4"
    },
    {
        "start": 10.964459016393443,
        "end": 11.546754098360656,
        "text": "colleagues",
        "diff_kind": "NO_DIFF",
        "uuid": "635964a1-df79-4a83-9743-a97628dfb514"
    },
    {
        "start": 11.546754098360656,
        "end": 11.72144262295082,
        "text": "who",
        "diff_kind": "NO_DIFF",
        "uuid": "8df5624e-18ff-46bc-8495-99618b0a0402"
    },
    {
        "start": 11.72144262295082,
        "end": 11.896131147540984,
        "text": "may",
        "diff_kind": "NO_DIFF",
        "uuid": "9f2de2e5-af59-4a94-83fd-7dff416d2316"
    },
    {
        "start": 11.896131147540984,
        "end": 12.012590163934426,
        "text": "or",
        "diff_kind": "NO_DIFF",
        "uuid": "ebba8c00-290c-43c1-b25d-e859d3f23821"
    },
    {
        "start": 12.012590163934426,
        "end": 12.187278688524591,
        "text": "may",
        "diff_kind": "NO_DIFF",
        "uuid": "d99438a9-2b45-4921-b674-78b054f042fa"
    },
    {
        "start": 12.187278688524591,
        "end": 12.361967213114754,
        "text": "not",
        "diff_kind": "NO_DIFF",
        "uuid": "2c6f8946-5319-4f4f-8dcf-d62c8412189b"
    },
    {
        "start": 12.361967213114754,
        "end": 12.478426229508198,
        "text": "be",
        "diff_kind": "NO_DIFF",
        "uuid": "59e95d1b-58be-4c02-8214-b77714d0f4ba"
    },
    {
        "start": 12.478426229508198,
        "end": 12.711344262295082,
        "text": "part",
        "diff_kind": "NO_DIFF",
        "uuid": "90ff8f94-0438-4967-b801-b03a15adccf4"
    },
    {
        "start": 12.711344262295082,
        "end": 12.827803278688526,
        "text": "of",
        "diff_kind": "NO_DIFF",
        "uuid": "257f1048-5124-4d4c-9360-195754e5d3be"
    },
    {
        "start": 12.827803278688526,
        "end": 13.06072131147541,
        "text": "your",
        "diff_kind": "NO_DIFF",
        "uuid": "68002a69-fb1a-40ae-b443-cb80342513cb"
    },
    {
        "start": 13.06072131147541,
        "end": 13.468327868852459,
        "text": "project",
        "diff_kind": "NO_DIFF",
        "uuid": "6d7aa467-bd50-4da3-b247-56db20462479"
    },
    {
        "start": 13.468327868852459,
        "end": 13.584786885245903,
        "text": "so",
        "diff_kind": "NO_DIFF",
        "uuid": "514309f3-0cf2-415a-b6e1-3f24d13f70f3"
    },
    {
        "start": 13.584786885245903,
        "end": 13.93416393442623,
        "text": "here's",
        "diff_kind": "NO_DIFF",
        "uuid": "71f1df8e-f46e-46fa-8fdc-180504738b6d"
    },
    {
        "start": 13.93416393442623,
        "end": 14.108852459016394,
        "text": "how",
        "diff_kind": "NO_DIFF",
        "uuid": "bdfd873f-53f1-43f3-b775-e61752ed1176"
    },
    {
        "start": 14.108852459016394,
        "end": 14.283540983606558,
        "text": "you",
        "diff_kind": "NO_DIFF",
        "uuid": "c75c12c9-5a29-4232-bc4c-f7d486cfe3e5"
    },
    {
        "start": 14.283540983606558,
        "end": 14.4,
        "text": "do",
        "diff_kind": "NO_DIFF",
        "uuid": "af6fe8c9-1035-4adf-8e0f-d8d1e450e0df"
    },
    {
        "start": 14.4,
        "end": 14.5216,
        "text": "it",
        "diff_kind": "NO_DIFF",
        "uuid": "a51e1177-21df-4a98-93b5-ce345c37734a"
    },
    {
        "start": 14.5216,
        "end": 14.764800000000001,
        "text": "when",
        "diff_kind": "NO_DIFF",
        "uuid": "4287620f-919f-41bd-90fd-f9db5b9bac33"
    },
    {
        "start": 14.764800000000001,
        "end": 15.1296,
        "text": "you're",
        "diff_kind": "NO_DIFF",
        "uuid": "c3420c81-dd0a-4932-b777-1157281f1deb"
    },
    {
        "start": 15.1296,
        "end": 15.2512,
        "text": "in",
        "diff_kind": "NO_DIFF",
        "uuid": "7d0d7ed2-7793-4d6a-b5e7-11112b3dbd4a"
    },
    {
        "start": 15.2512,
        "end": 15.494399999999999,
        "text": "your",
        "diff_kind": "NO_DIFF",
        "uuid": "35fc587b-b9e0-43d7-a6d7-2b8e7d069f5e"
    },
    {
        "start": 15.494399999999999,
        "end": 16.0416,
        "text": "dashboard",
        "diff_kind": "NO_DIFF",
        "uuid": "a1bcf4a3-d287-40ac-8b1b-bbe6da26f43f"
    },
    {
        "text": "*",
        "start": 16.032,
        "end": 16.128,
        "type": "WORD",
        "diff_kind": "INS_DEL",
        "uuid": "e4864a34-a93e-401f-a801-84a7c9b77b58"
    },
    {
        "start": 16.0416,
        "end": 16.1632,
        "text": "on",
        "diff_kind": "NO_DIFF",
        "uuid": "a2e14a32-49a7-473f-8599-fe4d9a9536da"
    },
    {
        "start": 16.1632,
        "end": 16.528,
        "text": "source",
        "diff_kind": "NO_DIFF",
        "uuid": "5224c193-fcb4-4ea0-b8ea-bb14f9aba64a"
    },
    {
        "start": 16.528,
        "end": 17.0144,
        "text": "platform",
        "diff_kind": "NO_DIFF",
        "uuid": "2f6f9fd6-973f-4c34-a4d6-dcdaa41b7cbc"
    },
    {
        "start": 17.0144,
        "end": 17.379199999999997,
        "text": "inside",
        "diff_kind": "NO_DIFF",
        "uuid": "43dfa843-7b50-4434-a912-6a9cb956adc8"
    },
    {
        "start": 17.379199999999997,
        "end": 17.6224,
        "text": "your",
        "diff_kind": "NO_DIFF",
        "uuid": "4f6ffa93-4c7c-4c0a-9829-3cd0502e01ae"
    },
    {
        "start": 17.6224,
        "end": 18.048,
        "text": "project",
        "diff_kind": "NO_DIFF",
        "uuid": "e2bcb0cf-5cac-4886-bb66-d3fc1e9a6a18"
    },
    {
        "start": 18.048,
        "end": 18.348,
        "text": "you",
        "diff_kind": "NO_DIFF",
        "uuid": "154fa41a-e930-461d-a9aa-56827d2bda3e"
    },
    {
        "start": 18.348,
        "end": 18.747999999999998,
        "text": "just",
        "diff_kind": "NO_DIFF",
        "uuid": "8511d39e-e046-4d52-a6c9-3e6400a63931"
    },
    {
        "start": 18.747999999999998,
        "end": 19.048,
        "text": "hit",
        "diff_kind": "NO_DIFF",
        "uuid": "30e5f642-d925-47f6-bd34-ec61de416cfa"
    },
    {
        "start": 19.048,
        "end": 19.348,
        "text": "the",
        "diff_kind": "NO_DIFF",
        "uuid": "d7c57493-b187-4777-9af3-89f38b3eb183"
    },
    {
        "start": 19.348,
        "end": 19.848,
        "text": "share",
        "diff_kind": "NO_DIFF",
        "uuid": "bcab645e-9bd0-47f2-9d6f-0282712ed802"
    },
    {
        "text": "*",
        "start": 19.824,
        "end": 20.544,
        "type": "WORD",
        "diff_kind": "INS_DEL",
        "uuid": "d31e8e98-0675-42d4-8c22-55ca323a320e"
    },
    {
        "start": 19.848,
        "end": 20.247999999999998,
        "text": "icon",
        "diff_kind": "NO_DIFF",
        "uuid": "96bae2aa-e0a0-487a-8074-e987b05294fa"
    },
    {
        "start": 20.247999999999998,
        "end": 20.848,
        "text": "select",
        "diff_kind": "NO_DIFF",
        "uuid": "0e71e626-db20-4a72-a5e2-44e615d206e3"
    },
    {
        "start": 20.848,
        "end": 21.148,
        "text": "the",
        "diff_kind": "NO_DIFF",
        "uuid": "dc3ad80a-9d9e-4b6d-a72f-022907c5b203"
    },
    {
        "start": 21.148,
        "end": 21.648,
        "text": "clips",
        "diff_kind": "NO_DIFF",
        "uuid": "b1b5e3f2-5459-452c-996c-221a17e6429a"
    },
    {
        "start": 21.648,
        "end": 21.94422857142857,
        "text": "that",
        "diff_kind": "NO_DIFF",
        "uuid": "02d80b26-fd80-480b-bbce-f20d458e9c6b"
    },
    {
        "start": 21.94422857142857,
        "end": 22.1664,
        "text": "you",
        "diff_kind": "NO_DIFF",
        "uuid": "ee8bbcac-9440-4d21-ac26-91f5fbc9cdfb"
    },
    {
        "start": 22.1664,
        "end": 22.46262857142857,
        "text": "want",
        "diff_kind": "NO_DIFF",
        "uuid": "29db8f66-b4cc-4f5d-95f8-b9f00025230c"
    },
    {
        "start": 22.46262857142857,
        "end": 22.610742857142856,
        "text": "to",
        "diff_kind": "NO_DIFF",
        "uuid": "18d4f759-4c62-4e6a-b861-d3f41acde766"
    },
    {
        "start": 22.610742857142856,
        "end": 22.98102857142857,
        "text": "share",
        "diff_kind": "NO_DIFF",
        "uuid": "c529dcf7-8566-4c63-8f9c-4c9e54ed2aa1"
    },
    {
        "start": 22.98102857142857,
        "end": 23.2032,
        "text": "hit",
        "diff_kind": "NO_DIFF",
        "uuid": "5967894f-5487-48da-aa37-abb78db97a7e"
    },
    {
        "start": 23.2032,
        "end": 23.425371428571427,
        "text": "the",
        "diff_kind": "NO_DIFF",
        "uuid": "daa71cbc-2d68-4252-87c3-9394668b91ed"
    },
    {
        "start": 23.425371428571427,
        "end": 23.79565714285714,
        "text": "share",
        "diff_kind": "NO_DIFF",
        "uuid": "8313791a-ffcd-47c7-a235-743104cc1d7d"
    },
    {
        "start": 23.79565714285714,
        "end": 24.24,
        "text": "button",
        "diff_kind": "NO_DIFF",
        "uuid": "27cd9638-741f-4b1d-8ad7-28eff553fcf6"
    }
];

let filtered = arrayToFilter.filter(obj => {
    let ins_del = (obj.diff_kind && obj.diff_kind === 'INS_DEL');
    return (obj.text !== '*' && !ins_del);
});

console.log("- Filtered and Mapped function/TX Result:\n", filtered.map(obj => {
    return {
        start: obj.start,
        end: obj.end,
        text: obj.text.toUpperCase()
    };
}));
