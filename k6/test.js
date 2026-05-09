import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        high_load: {
            executor: 'constant-arrival-rate',
            rate: 100000,           // 100k TPS 목표
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 5000,
            maxVUs: 20000,
        },
    },
};

const BASE_URL = 'http://host.docker.internal:8080';

export default function () {
    // 게시글 랜덤 조회
    const postId = Math.floor(Math.random() * 10000) + 1;

    const res = http.get(
        `${BASE_URL}/posts/query/find/${postId}`
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // TPS 유지용 (중요)
    sleep(0.001);
}

// import http from 'k6/http';
// import { sleep } from 'k6';
//
// export const options = {
//     vus: 100, // 동시 사용자 수 (100명)
//     duration: '30s', // 테스트 시간 (30초 동안)
//     thresholds: {},
//     ext: {
//         loadimpact: {
//             distribution: {},
//         },
//     },
// };
//
// export default function () {
//     http.get('http://localhost:8080/posts/query/find/1');
//     sleep(1);
// }


// // 조회
// export const options = {
//     scenarios: {
//         read_test: {
//             executor: 'constant-arrival-rate',
//             rate: 5000,   // 초당 5000
//             timeUnit: '1s',
//             duration: '1m',
//             preAllocatedVUs: 1000,
//         },
//     },
// };
//
// export default function () {
//     const postId = Math.floor(Math.random() * 100000) + 1;
//     http.get(`http://host.docker.internal:8080/api/posts/${id}`);
// }
//
// // 좋아요 (Kafka 부하 테스트)
// export const options = {
//     scenarios: {
//         like_test: {
//             executor: 'constant-arrival-rate',
//             rate: 2000,
//             timeUnit: '1s',
//             duration: '1m',
//             preAllocatedVUs: 500,
//         },
//     },
// };
//
// export default function () {
//     const postId = Math.floor(Math.random() * 100000) + 1;
//     http.post(`http://localhost:8080/api/posts/${postId}/like`);
// }
//
// // 조회수 (Kafka + Redis)
// export default function () {
//     const postId = Math.floor(Math.random() * 100000) + 1;
//     http.post(`http://localhost:8080/api/posts/${postId}/view`);
// }
//
// // 검색 (Elasticsearch)
// export default function () {
//     http.get(`http://localhost:8080/api/posts/search?keyword=test&type=title`);
// }
//
// // 실제 사용자 시나리오
// export default function () {
//     const postId = Math.floor(Math.random() * 100000) + 1;
//
//     http.get(`/api/posts`);
//     http.get(`/api/posts/${postId}`);
//     http.post(`/api/posts/${postId}/like`);
// }