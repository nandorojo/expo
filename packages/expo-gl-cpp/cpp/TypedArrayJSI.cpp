#include "TypedArrayJSI.h"
#include "TypedArrayJSC.h"

using Type = TypedArray::Type;

template<Type T>
using ContentType = TypedArray::ContentType<T>;

static std::unique_ptr<TypedArray> instance { nullptr };

std::unique_ptr<TypedArray>& TypedArray::get() {
  return instance;
};

void prepareTypedArrayApi(jsi::Runtime& runtime) {
  if (!runtime.global().getProperty(runtime, "HermesInternal").isUndefined()) {
    // TODO: setup hermes
  } else {
    instance = std::unique_ptr<TypedArray>(new JSCTypedArray());
  }
}

// class methods can't be both templated and virtual, so implementation
// needs to resolve TypedArray API types in runtime

template <Type T>
jsi::Value TypedArray::create(jsi::Runtime& runtime, std::vector<ContentType<T>> data) {
  switch (instance->vmName()) {
    case VMType::JSC:
      return reinterpret_cast<JSCTypedArray*>(instance.get())->create(runtime, data);
    default:
      throw std::runtime_error("typed array api not supported");
  }
}

void TypedArray::updateWithData(jsi::Runtime& runtime, const jsi::Value& val, std::vector<uint8_t> data) {
  switch (instance->vmName()) {
    case VMType::JSC:
      return reinterpret_cast<JSCTypedArray*>(instance.get())->updateWithData(runtime, val, data);
    default:
      throw std::runtime_error("typed array api not supported");
  }
}

template <Type T>
std::vector<ContentType<T>> TypedArray::fromJSValue(jsi::Runtime& runtime, const jsi::Value& val) {
  switch (instance->vmName()) {
    case VMType::JSC:
      return reinterpret_cast<JSCTypedArray*>(instance.get())->fromJSValue<T>(runtime, val);
    default:
      throw std::runtime_error("typed array api not supported");
  }
}

std::vector<uint8_t> TypedArray::rawFromJSValue(jsi::Runtime& runtime, const jsi::Value& val) {
  switch (instance->vmName()) {
    case VMType::JSC:
      return reinterpret_cast<JSCTypedArray*>(instance.get())->rawFromJSValue(runtime, val);
    default:
      throw std::runtime_error("typed array api not supported");
  }
}

Type TypedArray::typeFromJSValue(jsi::Runtime& runtime, const jsi::Value& val) {
  switch (instance->vmName()) {
    case VMType::JSC:
      return reinterpret_cast<JSCTypedArray*>(instance.get())->typeFromJSValue(runtime, val);
    default:
      throw std::runtime_error("typed array api not supported");
  }
}


