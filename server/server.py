from flask import Flask
from flask import Response
import os

app = Flask(__name__)

top_article_terms = '{"error" : "terms not found"}' 


@app.route('/GET/terms')
def rank():
	return Response(top_article_terms, mimetype='application/json')
	#return top_article_terms
	# return 'Hello World!'
	

if __name__ == '__main__':
	directory = os.path.join(os.path.dirname(__file__), "ranking")
	json_file = os.path.join(directory, "articleTerms.json")
	print(json_file)
	if os.path.exists(json_file):
		print("file exist")
		with open(json_file) as data_file:
			top_article_terms = data_file.read()
	else:
		print("no json file")
		
	app.run(host='0.0.0.0')