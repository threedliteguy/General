from flask import Flask, request, json
import redis  # pip install redis

app = Flask(__name__)

r = redis.Redis(host='localhost', port=6379, db=0)


@app.route("/ra", methods=['POST'])
def ra():
    j = request.get_json()
    v = []
    for i in j:
       x = {"user":i,"status":r.get(i)}  
       v.append(x)

    response = app.response_class(
        response=json.dumps(v),
        status=200,
        mimetype='application/json'
    )

    return response


if __name__ == '__main__':
    app.run(debug=True, port=5000)

