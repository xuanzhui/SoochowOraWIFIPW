__author__ = 'xuanzhui'
# http://docs.python-requests.org/en/latest/user/quickstart/

import requests, re


def printDebugInfo(resp):
    print('respond status code : ', resp.status_code)
    print('respond cookies : ', resp.cookies)
    print('respond headers : ', resp.headers)
    print('respond content : ', resp.content)

def parseHiddenInputValues(ssopage):
    data_params = {}

    params_list = re.findall(b'input type="hidden" name="(.*?)" value="(.*?)"', ssopage)

    for param in params_list:
        data_params[param[0].decode()] = param[1].decode()

    return data_params

debugFlag = True

targeturl = 'https://gmp.oracle.com/captcha/files/airespace_pwd_apac.txt'

if debugFlag:
    print('-- step1 request target url --')
resp = requests.get(targeturl, allow_redirects=False)

if debugFlag:
    printDebugInfo(resp)

if debugFlag:
    print('-- step2 load redirected sso page --')
resp.headers['User-Agent'] = 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0'
resp = requests.get(resp.headers['location'], allow_redirects=False, headers=resp.headers)

ssopage = resp.content

if debugFlag:
    printDebugInfo(resp)

if debugFlag:
    print('-- step3 directly post login page --')

#resp.headers['User-Agent']='Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0'
resp = requests.post('https://login.oracle.com/mysso/signon.jsp',
                     data=parseHiddenInputValues(ssopage),
                     allow_redirects=False)

ssopage = resp.content

if debugFlag:
    printDebugInfo(resp)

if debugFlag:
    print('-- step4 post login page with username and password --')
data_params = parseHiddenInputValues(ssopage)

#TODO Set your own username and password
data_params['ssousername']='username'
data_params['password']='password'

#resp.headers['Host']='login.oracle.com'
#resp.headers['Accept-Encoding']='gzip, deflate'
#resp.headers['Content-Type']='application/x-www-form-urlencoded'
resp = requests.post('https://login.oracle.com/oam/server/sso/auth_cred_submit',
                     data=data_params,
                     allow_redirects=False)

if debugFlag:
    printDebugInfo(resp)

if debugFlag:
    print('-- step5 retrieve target content --')

resp = requests.get(resp.headers['location'])

if debugFlag:
    printDebugInfo(resp)

resp.content