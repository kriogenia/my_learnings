// Bring in Phoenix channels client library:
import {Socket} from "phoenix";

// And connect to the path in "lib/discuss_web/endpoint.ex". We pass the
// token for authentication. Read below how it should be used.
let socket = new Socket("/socket", {params: {token: window.userToken}});
socket.connect();

// Now that you are connected, you can join channels with a topic.
// Let's assume you have a channel with a topic named `room` and the
// subtopic is its id - in this case 42:
const createSocket = (topicId) => {
	return new Promise((resolve, reject) => {
		let channel = socket.channel(`comments:${topicId}`, {})
		return channel.join()
		  .receive("ok", resp => resolve({
			channel: channel,
			response: resp
		  }))
		  .receive("error", resp => reject(resp));
	});
};

export default createSocket;
