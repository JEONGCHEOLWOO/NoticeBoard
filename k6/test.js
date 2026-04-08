import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 10, // 동시에 10명
    duration: '30s', // 30초 동안
};

export default function () {
    http.get('http://localhost:8080/posts');
    sleep(1);
}