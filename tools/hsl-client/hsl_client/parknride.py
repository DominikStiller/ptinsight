import requests
import json
import concurrent.futures as cf

facilities = json.loads(requests.get('https://p.hsl.fi/api/v1/facilities').content)['results']
facilities.sort(key=lambda x: x['id'])
pool = cf.ThreadPoolExecutor()

def print_util(id):
	utilization = json.loads(requests.get(f'https://p.hsl.fi/api/v1/facilities/{id}/prediction?after=90').content)
	if utilization:
		print(f'{id=} {utilization}')

for f in facilities:
	pool.submit(print_util, f['id'])
