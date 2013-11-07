import urllib.request
import urllib.error
import base64
from bs4 import BeautifulSoup
import sys
import os
import codecs
import hashlib
import subprocess
import sys

# def extractBBC(link):
	# page = urllib.request.urlopen(link)
	# soup = BeautifulSoup(page)
	# return soup.select(".article p")

# def extractSkySports(link):
	# page = urllib.request.urlopen(link)
	# soup = BeautifulSoup(page)
	# return soup.select("div.v5-art-body p")

def extractArticle(link, type):
	if (type == 'yahoo'):
		link = link[link.find('*')+1:]
		link = link.replace("%3A", ':')
		
	print("Extracting from " + link)
	try:
		page = urllib.request.urlopen(link)
		soup = BeautifulSoup(page)
		# print(soup.prettify() + "lol")
		if (type == 'skysports'):
			lines =  soup.select(".article-body p")
			if not lines: #empty list
				lines =  soup.select(".v5-art-body p")
		elif (type == 'yahoo'):
			lines =  soup.select("div.body.yom-art-content.clearfix p")
			# print(lines)
		elif (type == 'bbc'):
			lines = soup.select(".article p")
			if not lines: #empty list
				lines =  soup.select("div#meta-information p")
		elif (type == 'espn'):
			lines = soup.select(".article p")
		else:
			lines =  soup.select("p")
	except urllib.error.URLError:
		print("URL Error")
		return ""
	article = ""
	for line in lines:
		article =  article + line.get_text()
	# print(article)
	return article
		
def scrapXML(link):
	page = urllib.request.urlopen(link)
	soup = BeautifulSoup(page)
	#for text in soup.find_all('pubDate'):
	#	print(text.get_text())
		#extractBBC(text.getText())
	return soup.select('item link')
	# for text in soup.select('item link'):
		# print(text.get_text())
		# extractBBC(text.getText())			

# def beginCrawl(xmlLink, type):
	# string = ''
	# links = scrapXML(xmlLink)
	
	# for link in links:
		# print(link.get_text())
		# for line in extractArticle(link.getText(), type):
			# string = string + line.getText()
	
	# return string
		
def writeToFile(url, category, text):
	m = hashlib.md5()
	filename = category + '_' + hashlib.md5(str.encode(url)).hexdigest() + '.txt'
	directory = os.path.join(os.path.join(os.path.dirname(__file__), "articles"), category)
	if not os.path.exists(directory):
		os.makedirs(directory)
	filepath = os.path.join(directory,filename)
	# with codecs.open(filepath, 'w', 'utf-8') as the_file:
		# print("Writing article: " + filename)
		# the_file.write(text)

	if not os.path.exists(filepath):
		with codecs.open(filepath, 'w', 'utf-8') as the_file:
			print("Writing article: " + filename)
			the_file.write(text)
		


def scrapArticles():
	string = ''
	sources = [("http://sports.yahoo.com/mlb/rss.xml", 'yahoo', 'baseball'),("http://sports.yahoo.com/nfl/rss.xml", 'yahoo', 'rugby'), ("http://sports.yahoo.com/nba/rss.xml", 'yahoo', 'basketball'),("http://sports.yahoo.com/nhl/rss.xml", 'yahoo', 'hockey'),("http://sports.yahoo.com/nascar/rss.xml", 'yahoo', 'motorsports'),("http://sports.yahoo.com/golf/rss.xml", 'yahoo', 'golf'),("http://sports.yahoo.com/ufc/rss.xml", 'yahoo', 'mma'),("http://sports.yahoo.com/box/rss.xml", 'yahoo', 'boxing'),("http://sports.yahoo.com/ncaa/basketball/rss.xml", 'yahoo', 'basketball'),("http://sports.yahoo.com/ncaa/womens-basketball/rss.xml", 'yahoo', 'basketball'),("http://sports.yahoo.com/ncaabb/rss.xml", 'yahoo', 'baseball'),("http://sports.yahoo.com/wnba/rss.xml", 'wnba', 'basketball'),("http://sports.yahoo.com/ncaa/football/rss.xml", 'yahoo', 'rugby'),("http://sports.yahoo.com/irl/rss.xml", 'yahoo', 'motorsports'),("http://sports.yahoo.com/mls/rss.xml", 'yahoo', 'soccer'),("http://sports.yahoo.com/ski/rss.xml", 'yahoo', 'ski'),("http://sports.yahoo.com/sc/rss.xml", 'yahoo', 'cycling'),("http://sports.yahoo.com/rah/rss.xml", 'yahoo', 'horse_racing'),("http://www.skysports.com/rss/0,20514,12375,00.xml", 'skysports', 'basketball'), ("http://www.skysports.com/rss/0,20514,12183,00.xml", 'skysports', 'boxing'), ("http://www.skysports.com/rss/0,20514,12140,00.xml", 'skysports', 'cricket'), ("http://www.skysports.com/rss/0,20514,12341,00.xml", 'skysports', 'cricket'), ("http://www.skysports.com/rss/0,20514,12123,00.xml", 'skysports', 'cricket'), ("http://www.skysports.com/rss/0,20514,15264,00.xml", 'skysports', 'cycling'), ("http://www.skysports.com/rss/0,20514,12433,00.xml", 'skysports', 'motorsports'), ("http://www.skysports.com/rss/0,20514,12061,00.xml", 'skysports', 'golf'),("http://www.skysports.com/rss/0,20514,12230,00.xml", 'skysports', 'golf'),("http://www.skysports.com/rss/0,20514,12176,00.xml", 'skysports', 'golf'),("http://www.skysports.com/rss/0,20514,12232,00.xml", 'skysports', 'golf'),("http://www.skysports.com/rss/0,20514,15773,00.xml", 'skysports', 'motorsports'),("http://www.skysports.com/rss/0,20514,15776,00.xml", 'skysports', 'motorsports'),("http://www.skysports.com/rss/0,20514,15778,00.xml", 'skysports', 'motorsports'),("http://www.skysports.com/rss/0,20514,12415,00.xml", 'skysports', 'netball'),("http://www.skysports.com/rss/0,20514,12118,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12426,00.xml", 'skysports', 'horse_racing'),("http://www.skysports.com/rss/0,20514,12196,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12331,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,19133,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12332,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12321,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12333,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12334,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12336,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,16616,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12337,00.xml", 'skysports', 'rugby'),("http://www.skysports.com/rss/0,20514,12268,00.xml", 'skysports', 'motorsports'),("http://www.skysports.com/rss/0,20514,12243,00.xml", 'skysports', 'snooker'),("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/cricket/rss.xml", 'bbc', 'cricket'), ("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/rugby_union/rss.xml", 'bbc', 'rugby'), ("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/rugby_league/rss.xml", 'bbc', 'rugby'), ("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/golf/rss.xml", 'bbc', 'golf'), ("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/motorsport/rss.xml", 'bbc', 'motorsports'),(" http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/boxing/rss.xml", 'bbc', 'boxing'), ("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/athletics/rss.xml", 'bbc', 'athletics'), ("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/other_sports/snooker/rss.xml", 'bbc', 'snooker'),("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/other_sports/horse_racing/rss.xml", 'bbc', 'horse_racing'),("http://newsrss.bbc.co.uk/rss/sportonline_uk_edition/other_sports/cycling/rss.xml", 'bbc', 'cycling'), ('http://www.skysports.com/rss/0,20514,11988,00.xml', 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11996,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,12110,00.xml", 'skysports', 'tennis'), ("http://www.skysports.com/rss/0,20514,11881,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11688,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11945,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,13864,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11906,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11959,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11966,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,19692,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,12010,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11981,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11719,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11750,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11800,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11095,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11661,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11827,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,12003,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11973,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11781,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,11854,00.xml", 'skysports', 'soccer'), ("http://www.skysports.com/rss/0,20514,12098,00.xml", 'skysports', 'soccer'), ("http://sports.yahoo.com/tennis/rss.xml", 'yahoo', 'tennis'), ("http://sports.espn.go.com/espn/rss/tennis/news", 'espn', 'tennis'), ("http://feeds.bbci.co.uk/sport/0/tennis/rss.xml?edition=uk", 'bbc', 'tennis'), ("http://feeds.bbci.co.uk/sport/0/football/rss.xml?edition=uk", 'bbc', 'soccer'), ("http://soccernet.espn.go.com/rss/news", 'espn', 'soccer'),]

	# sources = [("http://feeds.bbci.co.uk/sport/0/tennis/rss.xml?edition=uk", 'bbc', 'tennis')]

	# sources = [("http://www.skysports.com/rss/0,20514,11945,00.xml", 'skysports', 'soccer')]
	for source in sources:
		# string = beginCrawl(source[0], source[1])
		# url, category, article
		# writeToFile(source[0], source[2], string)
		xmlSource = source[0]
		type = source[1]
		category = source[2]
		for link in scrapXML(xmlSource):
			url = link.get_text()
			article = extractArticle(url, type)
			# print('here')
			if (len(article) > 0):
				# print('here')
				writeToFile(url, category, article)

				
def runLucene():
	# its win32, maybe there is win64 too?
	is_windows = sys.platform.startswith('win')
	#java -cp .;*; Lucene 
	if is_windows:
		subprocess.call(["java", "-cp", ".;*;", "Lucene"])
	else:
		subprocess.call(["java", "-cp", ".:*;", "Lucene"])
	
scrapArticles()
runLucene()

		
#print (soup.get_text().encode('cp850', errors='replace'))


