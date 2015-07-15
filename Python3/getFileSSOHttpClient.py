#!/usr/bin/env python3

__author__ = 'xuanzhui'

import http.client
import re
import urllib.parse

class GetFileSSOHttpClient:
    def __init__(self): 
        self.host=''
        self.filename=''
        self.cookie_map={}
        self.cookie_str=''
        self.pagecontent=''
        self.headers = {"User-Agent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0",
                   "Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
        self.debugFlag=False
        self.showStep=False


    def setShowStepFlag(self, showStep):
        self.showStep=showStep


    def setShowDebugFlag(self, debugFlag):
        self.debugFlag=debugFlag


    def handleCookie(self, new_cookie_str):

        cookies=new_cookie_str.split(';')
        for cookie in cookies:
            items=cookie.split('=')
            if len(items)==2:
                self.cookie_map[items[0].strip()]=items[1].strip()

        self.cookie_str=''
        for item in self.cookie_map.items():
            self.cookie_str+=item[0]+'='+item[1]+';'

        self.cookie_str = self.cookie_str[:-1]


    def parseLocation(self, location):
        mat=re.search(r'https?://(.*?)/(.*)',location)
        if mat:
            self.host=mat.group(1)
            self.filename='/'+mat.group(2)
        else:
            self.host = None
            self.filename = None


    def parseHiddenInputValues(self):
        params_list=re.findall(b'input type="hidden" name="(.*?)" value="(.*?)"',self.pagecontent)

        data_params={}
        
        for param in params_list:
            data_params[param[0].decode()]=param[1].decode()

        return data_params


    def printDebugInfo(self, resp):
        if self.cookie_str:
            print('cookie string:',self.cookie_str)
        if self.host:
            print('host:',self.host)
        if self.filename:
            print('requested filename:',self.filename)
        if resp:
            print('response status and reason:',resp.status,resp.reason)
            print('headers:',resp.getheader)
        if self.pagecontent:
            print('pagecontent:',self.pagecontent)
        

    #first request
    def firstreq(self):
        if self.showStep:
            print('-- directly request target page --')
            
        conn = http.client.HTTPSConnection("gmp.oracle.com")
        conn.request("GET", "/captcha/files/airespace_pwd_apac.txt")
        resp = conn.getresponse()

        if resp.getheader('Set-Cookie'):
            self.handleCookie(resp.getheader('Set-Cookie'))

        if resp.getheader('Location'):
            self.parseLocation(resp.getheader('Location'))

        self.pagecontent=resp.read()

        if self.debugFlag:
            self.printDebugInfo(resp)

        resp.close()


    #redirect to SSO
    def redrect2SSO(self):
        if self.showStep:
            print('-- redirect to SSO --')
            
        conn = http.client.HTTPSConnection(self.host)
        conn.request("GET", self.filename)
        resp = conn.getresponse()

        if resp.getheader('Set-Cookie'):
            self.handleCookie(resp.getheader('Set-Cookie'))

        if resp.getheader('Location'):
            self.parseLocation(resp.getheader('Location'))

        self.pagecontent=resp.read()

        if self.debugFlag:
            self.printDebugInfo(resp)

        resp.close()


    def firstSSOreq(self):
        if self.showStep:
            print('-- first SSO request --')
            
        data_params=self.parseHiddenInputValues()

        conn = http.client.HTTPSConnection('login.oracle.com')
        self.headers["Cookie"] = self.cookie_str
        post_data=urllib.parse.urlencode(data_params)
        conn.request("POST", '/mysso/signon.jsp', post_data, self.headers)

        resp = conn.getresponse()

        if resp.getheader('Set-Cookie'):
            self.handleCookie(resp.getheader('Set-Cookie'))

        '''
        if resp.getheader('Location'):
            host,filename=parseLocation(resp.getheader('Location'))
        '''

        self.pagecontent=resp.read()

        if self.debugFlag:
            self.printDebugInfo(resp)

        resp.close()


    def secondSSOreq(self):
        if self.showStep:
            print('-- second sso request --')
            
        data_params=self.parseHiddenInputValues()

        #TODO Set your own username and password
        data_params['ssousername']='username'
        data_params['password']='password'

        conn = http.client.HTTPSConnection('login.oracle.com')
        self.headers["Cookie"] = self.cookie_str
        post_data=urllib.parse.urlencode(data_params)
        conn.request("POST", '/oam/server/sso/auth_cred_submit', post_data, self.headers)

        resp = conn.getresponse()

        if resp.getheader('Set-Cookie'):
            self.handleCookie(resp.getheader('Set-Cookie'))

        if resp.getheader('Location'):
            self.parseLocation(resp.getheader('Location'))

        self.pagecontent=resp.read()

        if self.debugFlag:
            self.printDebugInfo(resp)

        resp.close()


    def afterSSOsucc(self):
        if self.showStep:
            print('-- after sso successful --')
            
        conn = http.client.HTTPSConnection(self.host)
        self.headers['Cookie']=self.cookie_str
        conn.request("GET", self.filename, headers=self.headers)
        resp = conn.getresponse()
        if resp.getheader('Set-Cookie'):
            self.handleCookie(resp.getheader('Set-Cookie'))

        if resp.getheader('Location'):
            self.parseLocation(resp.getheader('Location'))

        self.pagecontent=resp.read()

        if self.debugFlag:
            self.printDebugInfo(resp)

        resp.close()


    def finalRes(self):
        if self.showStep:
            print('-- final target resource --')
            
        conn = http.client.HTTPSConnection(self.host)
        self.headers['Cookie']=self.cookie_str
        conn.request("GET", self.filename, headers=self.headers)
        resp = conn.getresponse()
        if resp.getheader('Set-Cookie'):
            self.handleCookie(resp.getheader('Set-Cookie'))

        if resp.getheader('Location'):
            self.parseLocation(resp.getheader('Location'))

        self.pagecontent=resp.read()

        if self.debugFlag:
            self.printDebugInfo(resp)

        resp.close()

    def parsepage(self):
        if self.showStep:
            print('-- parse userid and password --')

        targetcont=self.pagecontent.decode().replace('\n','')

        mat=re.search('Userid:\s*(.*?)Password:\s*(.*?)Generated at', targetcont)

        if mat:
            userid=mat.group(1)
            pw=mat.group(2)

            if self.debugFlag:
                print('Userid :', userid)
                print('Password :', pw)

            return userid,pw

        else:
            return None, None


    def pwfullsteps(self):
        self.firstreq()
        self.redrect2SSO()
        self.firstSSOreq()
        self.secondSSOreq()
        self.afterSSOsucc()
        self.finalRes()
        return self.parsepage()

if __name__=='__main__':
    test=GetFileSSOHttpClient()
    test.setShowDebugFlag(True)
    test.setShowStepFlag(True)
    print(test.pwfullsteps())
