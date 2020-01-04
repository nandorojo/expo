#pragma once

#include "TypedArrayJSI.h"

class JSCTypedArray : public TypedArray {
public:
  virtual ~JSCTypedArray() { };

  template <Type T>
  jsi::Value create(jsi::Runtime& runtime, std::vector<ContentType<T>> data);

  void updateWithData(jsi::Runtime& runtime, const jsi::Value& val, std::vector<uint8_t> data);

  template <Type T>
  std::vector<ContentType<T>> fromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

  std::vector<uint8_t> rawFromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

  Type typeFromJSValue(jsi::Runtime& runtime, const jsi::Value& val);
private:
  bool usingTypedArrayHack = false;

  VMType vmName() override {
    return VMType::JSC;
  }
};
