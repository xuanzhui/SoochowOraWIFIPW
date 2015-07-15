#!/usr/bin/env python3

__author__ = 'xuanzhui'

import getFileSSOHttpClient
import qrcode
from PIL import Image

class GenerateWifiPWQR:
	def __init__(self, imgname = 'setup your default img path'):
		self.pw = None;
		self.imgname = imgname

	def generate(self, addicon = True):
		#request three times
		for i in range(3):
			self.pw = getFileSSOHttpClient.GetFileSSOHttpClient().pwfullsteps()[1]

			if self.pw:
				break

		if not self.pw:
			return False

		qr = qrcode.QRCode(
		    version=1,
		    error_correction=qrcode.constants.ERROR_CORRECT_L,
		    box_size=12,
		    border=4,
		)
		qr.add_data(self.pw)
		qr.make(fit=True)

		img = qr.make_image()

		#add oracle icon
		if addicon:
			img = img.convert("RGBA")
	 
			icon = Image.open("setup your icon path")
			 
			img_w, img_h = img.size
			factor = 8
			size_w = int(img_w / factor)
			size_h = int(img_h / factor)
			 
			icon_w, icon_h = icon.size
			if icon_w > size_w:
			    icon_w = size_w
			if icon_h > size_h:
			    icon_h = size_h
			icon = icon.resize((icon_w, icon_h), Image.ANTIALIAS)
			 
			w = int((img_w - icon_w) / 2)
			h = int((img_h - icon_h) / 2)
			img.paste(icon, (w, h))

		img.save(self.imgname)

		return True

if __name__ == '__main__':
	wifiqr = GenerateWifiPWQR()
	wifiqr.generate()
	print('password:', wifiqr.pw)
